package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.ReplayServiceRLRP;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static org.junit.Assert.fail;

public class ReplayServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * - @Mock annotation creates a mock object for the specified class or interface.
     * Simulates the definition of an object and populates it.
     */
    private ReplayService replayService;
    private File replayFile;
    private FolderDAO mockFolderDAO;

    @Before
    public void setUp() {

        // get the MatchDAO component from the spring framework
        mockFolderDAO = new UserFolderDAO("mockParser", "mockFiles");
        replayService = new ReplayServiceRLRP(mockFolderDAO);
        replayFile = new File(getClass().getResource("/testReplays/test.replay").getFile());
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(mockFolderDAO.getFileDirectory());
            FileUtils.deleteDirectory(mockFolderDAO.getParserDirectory());
        } catch (IOException e) {
            LOG.error("Exception while tearing Down Replay Service test", e);
        }
    }


    @Test
    public void testReplayParsing() {
        File jsonFile = null;
        try {
            jsonFile = replayService.parseReplayFileToJson(replayFile);
        } catch (FileServiceException e) {
            fail();
        }
        Assert.assertTrue(jsonFile.exists());
        Assert.assertEquals("json",FilenameUtils.getExtension(jsonFile.getName()));
    }

}