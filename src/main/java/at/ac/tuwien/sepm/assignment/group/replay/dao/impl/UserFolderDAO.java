package at.ac.tuwien.sepm.assignment.group.replay.dao.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Date;

@Component
public class UserFolderDAO implements FolderDAO {

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File replayToJsonParser;
    private File parserDirectory;
    private File fileDirectory;


    public UserFolderDAO(String parserDir, String fileDir) {
        parserDirectory = setupDirectory(parserDir);
        fileDirectory = setupDirectory(fileDir);
    }

    //Default Folder Names
    public UserFolderDAO() {
        parserDirectory = setupDirectory("parser");
        fileDirectory = setupDirectory("files");
    }

    @Override
    public File setupDirectory(String folder) {
        LOG.trace("Called - setupDirectory");

        File directory = new File(System.getProperty("user.home"), "qse01ReplayParser/" + folder);

        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOG.debug("{} Directory setup successfully", folder);
            } else {
                LOG.error("Error setting up {} Directory", folder);
            }
        }
        return directory;
    }

    @Override
    public File copyReplayFile(File replayFile) throws FilePersistenceException {
        LOG.trace("Called - copyReplayFile");
        if (!FilenameUtils.getExtension(replayFile.getName()).equals("replay")) {
            throw new FilePersistenceException("File not .replay");
        }
        File file;
        do {
            String uniqueName = createUniqueName();
            file = new File(fileDirectory, uniqueName);
            try {
                Files.copy(replayFile.getAbsoluteFile().toPath(), file.getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new FilePersistenceException("Caught exception while copying replay file to directory", e);
            }
        } while (!file.exists());

        return file;
    }

    private File extractFile(String fileName) throws FilePersistenceException {
        LOG.trace("Called - extractFile");

        File file = new File(parserDirectory, fileName);

        if (!file.exists()) {
            InputStream fileStream = getClass().getResourceAsStream("/replayParser/" + fileName);
            try {
                Files.copy(fileStream, file.getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new FilePersistenceException("Caught Exception while setting up parser", e);
            }
        }
        return file;
    }

    @Override
    public File createJsonFile(File replayFile) {
        LOG.trace("Called - createJsonFile");

        return new File(fileDirectory, FilenameUtils.getBaseName(replayFile.getName()) + ".json");
    }

    @Override
    public File getParserDirectory() {
        return parserDirectory;
    }

    @Override
    public File getFileDirectory() {
        return fileDirectory;
    }

    @Override
    public File getParser(String[] necessaryFiles) throws FilePersistenceException {
        LOG.trace("Called - getParser");
        if (replayToJsonParser == null || !replayToJsonParser.exists()) {
            replayToJsonParser = setupParser(necessaryFiles);
        }
        return replayToJsonParser;
    }


    /**
     * Method that extracts all the parser files from the properties to the parserDirectory folder.
     * {@link UserFolderDAO#parserDirectory} has to be setup before the method is called.
     *
     * @return The File (executable) that is used to parse .replay files
     */
    private File setupParser(String[] necessaryFiles) throws FilePersistenceException {
        LOG.trace("Called - setupParser");

        File file = null;

        for (String fileName : necessaryFiles) {
            file = extractFile(fileName);
        }

        return file;
    }


    /**
     * Method to create a unique filename
     *
     * @return a random Filename for the replay file
     */
    private String createUniqueName() {
        LOG.trace("Called - createUniqueName");

        long millis = System.currentTimeMillis();
        String datetime = new Date().toString();
        datetime = datetime.replace(" ", "");
        datetime = datetime.replace(":", "");
        String rndchars = RandomStringUtils.randomAlphanumeric(8);
        return rndchars + "_" + datetime + "_" + millis + ".replay";
    }


}
