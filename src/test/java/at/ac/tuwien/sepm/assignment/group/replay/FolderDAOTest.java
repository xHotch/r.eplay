package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * @author Philipp Hochhauser
 */
@RunWith(MockitoJUnitRunner.class)
public class FolderDAOTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    /**
     * - @Mock annotation creates a mock object for the specified class or interface.
     * Simulates the definition of an object and populates it.
     */
    private FolderDAO folderDAO;
    private String parserDir;
    private String fileDir;
    private File replayFile;


    @Before
    public void setUp() {
        parserDir = "testParserDir";
        fileDir = "testFileDir";
        folderDAO = new UserFolderDAO(parserDir, fileDir);
        replayFile = new File(getClass().getResource("/testReplays/test.replay").getFile());
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(folderDAO.getFileDirectory());
            FileUtils.deleteDirectory(folderDAO.getParserDirectory());
        } catch (IOException e) {
            LOG.error("Exception while tearing Down Replay Service test", e);
        }
    }


    @Test
    public void testFolderCreation() {
        Assert.assertEquals(parserDir, folderDAO.getParserDirectory().getName());
        Assert.assertEquals(fileDir, folderDAO.getFileDirectory().getName());
    }

    @Test
    public void testJsonCreation() {
        File jsonFile = folderDAO.createJsonFile(replayFile);

        Assert.assertEquals(FilenameUtils.getBaseName(replayFile.getName()), FilenameUtils.getBaseName(jsonFile.getName()));
        Assert.assertEquals(jsonFile.getParent(), folderDAO.getFileDirectory().getAbsolutePath());
    }

    @Test
    public void testCopyReplayFile() throws FilePersistenceException {
        File copiedFile = folderDAO.copyReplayFile(replayFile);


        Assert.assertTrue(copiedFile.exists());
        Assert.assertEquals(copiedFile.getParent(), folderDAO.getFileDirectory().getAbsolutePath());
    }

    @Test(expected = FilePersistenceException.class)
    public void copyWrongFileFormat() throws FilePersistenceException {
        File randomExeFile = new File("test.exe");

        folderDAO.copyReplayFile(randomExeFile);
    }
}
