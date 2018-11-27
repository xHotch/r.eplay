package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.universe.exceptions.ServiceException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Date;

/**
 * Service Class that parses .replay files using Rocket League Replay Parser
 * @see  <a href="https://github.com/jjbott/RocketLeagueReplayParser">https://github.com/jjbott/RocketLeagueReplayParser</a>
 *
 * @author Philipp Hochhauser
 */

@Service
public class ReplayServiceRLRP implements ReplayService {

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File replayToJsonParser;
    private File parserDirectory;
    private File fileDirectory;
    private String parserDir;
    private String fileDir;

    public ReplayServiceRLRP() {
        parserDir="parser";
        fileDir="files";
        parserDirectory=setupUserDirectory(parserDir);
        fileDirectory=setupUserDirectory(fileDir);
        replayToJsonParser=setupParser();
    }

    public ReplayServiceRLRP(String parserDir, String fileDir) {
        this.parserDir=parserDir;
        this.fileDir=fileDir;
        parserDirectory=setupUserDirectory(parserDir);
        fileDirectory=setupUserDirectory(fileDir);
        replayToJsonParser=setupParser();
    }

    public File getParserDirectory() {
        if (parserDirectory==null){
            setupUserDirectory(parserDir);
        }
        return parserDirectory;
    }

    public File getFileDirectory() {
        if (fileDirectory==null){
            setupUserDirectory(fileDir);
        }
        return fileDirectory;
    }

    @Override
    public File parseReplayFileToJson(File file) throws ServiceException {

        File replayFile = copyReplayFile(file);
        File jsonFile;
        try {
            String[] cmd = {replayToJsonParser.getAbsolutePath(), replayFile.getAbsolutePath()};
            Process proc = new ProcessBuilder(cmd).start();

            StreamReader errorSteam = new StreamReader(proc.getErrorStream(), "ERROR");

            errorSteam.start();
            jsonFile = new File(parserDirectory, FilenameUtils.getBaseName(replayFile.getName()) + ".json");

            Files.copy(proc.getInputStream(),jsonFile.getAbsoluteFile().toPath());

            int exitVal = proc.waitFor();
            LOG.debug("Parser process finished with exitValue : {} ", exitVal);


            if (exitVal!=0){
                LOG.error("Could not parse replay file: {}", replayFile.getAbsolutePath());
                throw new IllegalArgumentException("Could not parse replay file" + replayFile.getAbsolutePath());
            }

        } catch (Exception e){
            LOG.error("Cought Exception while parsing .replay file : {}", e.getMessage());
            throw new ServiceException("Cought Exception while parsing .replay file : {}", e);
        }

        LOG.debug("Created JSON File at: {}", jsonFile.getAbsolutePath());
        return jsonFile;
    }


    /**
     * Method that sets up a Directory in the user's home Directory
     * @return the created Directory
     */
    private File setupUserDirectory(String folder){
        File directory = new File(System.getProperty("user.home"),"qse01ReplayParser/" + folder);

        if (!directory.exists()) {
            if (directory.mkdirs()){
                LOG.debug("{} Directory setup successfully", folder);
            } else {
                LOG.error("Error setting up {} Directory", folder);
            }
        }
        return directory;
    }

    /**
     * Method that extracts all the parser files from the properties to the parserDirectory folder.
     * {@link ReplayServiceRLRP#parserDirectory} has to be setup before the method is called.
     *
     * @return The File (executable) that is used to parse .replay files
     */
    private File setupParser(){
        //Last File (.exe) is returned
        String[] necesarryFiles = new String[]{"CommandLine.dll", "KellermanSoftware.Compare-NET-Objects.dll", "Newtonsoft.Json.dll","RocketLeagueReplayParser.dll", "RocketLeagueReplayParser.exe" };

        File file = null;

        for (String fileName : necesarryFiles){
             file = new File(parserDirectory, fileName);

            if (!file.exists()) {
                InputStream fileStream = getClass().getResourceAsStream("/replayParser/" + fileName);
                try {
                    Files.copy(fileStream,file.getAbsoluteFile().toPath());
                } catch (IOException e){
                    LOG.error("Cought Exception while setting up parser: {}", e.getMessage());
                }
            }
        }
        return file;
    }

    /**
     * Method that copies a .replay file into the {@link ReplayServiceRLRP#fileDirectory} folder.
     * Assigns a unique name to the copied file.
     *
     * @throws IllegalArgumentException if the file is not a .replay file
     * @return The file after it has been copied.
     */
    private File copyReplayFile(File replayFile){
        if (!FilenameUtils.getExtension(replayFile.getName()).equals("replay")){
            throw new IllegalArgumentException("File not .replay");
        }
        File file;
        do {
            String uniqueName = createUniqueName();
            file = new File(fileDirectory, uniqueName);
            try {
                Files.copy(replayFile.getAbsoluteFile().toPath(),file.getAbsoluteFile().toPath());
            } catch (IOException e){
                LOG.error("Cought exception while copying replay file to directory");
            }
        } while (!file.exists());

        return file;
    }

    /**
     * Method to create a unique filename
     * @return a random Filename for the replay file
     */
    private String createUniqueName(){
        long millis = System.currentTimeMillis();
        String datetime = new Date().toString();
        datetime = datetime.replace(" ", "");
        datetime = datetime.replace(":", "");
        String rndchars = RandomStringUtils.randomAlphanumeric(8);
        return rndchars + "_" + datetime + "_" + millis +".replay";
    }

    /**
     * Class to simultaneously read error output while parsing .replay file
     *
     * @author Philipp Hochhauser
     */
    private class StreamReader extends Thread
    {
        InputStream is;
        String type;

        StreamReader(InputStream is, String type)
        {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run()
        {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                if (type.equals("ERROR"))
                    while ((line = br.readLine()) != null){
                        LOG.error(line);
                    }
            } catch (IOException ioe)
            {
                LOG.error("Cought IOException while reading output from process");
            }
        }
    }

}
