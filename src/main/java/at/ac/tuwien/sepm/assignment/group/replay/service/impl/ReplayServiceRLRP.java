package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;

/**
 * Service Class that parses .replay files using Rocket League Replay Parser
 *
 * @author Philipp Hochhauser
 * @see <a href="https://github.com/jjbott/RocketLeagueReplayParser">https://github.com/jjbott/RocketLeagueReplayParser</a>
 */

@Service
public class ReplayServiceRLRP implements ReplayService {

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File replayToJsonParser;
    private FolderDAO folderDAO;


    public ReplayServiceRLRP(FolderDAO folderDAO) {
        this.folderDAO = folderDAO;
    }


    @Override
    public File parseReplayFileToJson(File replayFile) throws FileServiceException {
        LOG.trace("Called - parseReplayFileToJson");

        File jsonFile = null;
        try {
            if (replayToJsonParser == null || !replayToJsonParser.exists()) {
                //Last File (.exe) is returned
                String[] necessaryFiles = new String[]{"CommandLine.dll", "KellermanSoftware.Compare-NET-Objects.dll", "Newtonsoft.Json.dll", "RocketLeagueReplayParser.dll", "RocketLeagueReplayParser.Console.exe"};
                replayToJsonParser = folderDAO.getParser(necessaryFiles);
            }
            String extension = FilenameUtils.getExtension(replayFile.getName());
            if (!extension.equals("replay")) {
                throw new FileServiceException("Wrong file type: " + extension);
            }

            String[] cmd = {replayToJsonParser.getAbsolutePath(), replayFile.getAbsolutePath()};
            Process proc = new ProcessBuilder(cmd).start();

            StreamReader errorSteam = new StreamReader(proc.getErrorStream(), "ERROR");

            errorSteam.start();

            jsonFile = folderDAO.createJsonFile(replayFile);

            Files.copy(proc.getInputStream(), jsonFile.getAbsoluteFile().toPath());

            int exitVal = proc.waitFor();
            LOG.debug("Parser process finished with exitValue : {} ", exitVal);

            if (exitVal != 0) {
                throw new FileServiceException("Could not parse replay file" + replayFile.getAbsolutePath());
            }

        } catch (Exception e) {
            try {
                folderDAO.deleteFile(jsonFile);
            } catch (FilePersistenceException e1) {
                LOG.error("File error",e1);
            }
            throw new FileServiceException("Exception while parsing .replay to .json",e);
        }

        LOG.debug("Created JSON File at: {}", jsonFile.getAbsolutePath());
        return jsonFile;
    }

    @Override
    public File copyReplayFile(File inputFile) throws FileServiceException {
        String extension = FilenameUtils.getExtension(inputFile.getName());
        if (!extension.equals("replay")) {
            throw new FileServiceException("Wrong file type: " + extension);
        }
        try {
            return folderDAO.copyReplayFile(inputFile);
        } catch (FilePersistenceException e){
            throw new FileServiceException("Could not copy replay File", e);
        }
    }


    /**
     * Class to simultaneously read error output while parsing .replay file
     *
     * @author Philipp Hochhauser
     */
    private class StreamReader extends Thread {
        InputStream is;
        String type;

        StreamReader(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            LOG.trace("Called - StreamReader.run");

            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                if (type.equals("ERROR")) while ((line = br.readLine()) != null) {
                    LOG.error(line);
                }
            } catch (IOException ioe) {
                LOG.error("Cought IOException while reading output from process");
            }
        }
    }

}
