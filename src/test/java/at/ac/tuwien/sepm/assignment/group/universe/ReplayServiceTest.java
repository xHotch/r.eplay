package at.ac.tuwien.sepm.assignment.group.universe;

import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayServiceRLRP;
import at.ac.tuwien.sepm.assignment.group.universe.dao.UniverseDAO;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Answer;
import at.ac.tuwien.sepm.assignment.group.universe.dto.Question;
import at.ac.tuwien.sepm.assignment.group.universe.exceptions.PersistenceException;
import at.ac.tuwien.sepm.assignment.group.universe.service.SimpleUniverseService;
import at.ac.tuwien.sepm.assignment.group.universe.service.UniverseService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Philipp Hochhauser
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplayServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    /**
     * - @Mock annotation creates a mock object for the specified class or interface.
     * Simulates the definition of an object and populates it.
     */
    private ReplayService replayService;
    private File replayFile;
    private String parserDir;
    private String fileDir;



    @Before
    public void setUp(){
        parserDir = "testParserDir";
        fileDir = "testFileDir";
        replayService = new ReplayServiceRLRP(parserDir,fileDir);
        replayFile = new File(getClass().getResource("/testReplays/test.replay").getFile());
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(replayService.getFileDirectory());
            FileUtils.deleteDirectory(replayService.getParserDirectory());
        } catch (IOException e){
            LOG.error("Exception while tearing Down Replay Service test", e);
        }
    }

    @Test
    public void testReplayToJsonParser() throws Exception {
        File returnedFile = replayService.parseReplayFileToJson(replayFile);
        Assert.assertTrue(returnedFile.exists());
        File copiedFile = new File(replayService.getFileDirectory(),FilenameUtils.getBaseName(returnedFile.getName())+".replay");
        System.out.println(copiedFile.getAbsolutePath());
        Assert.assertTrue(copiedFile.exists());
    }

    @Test
    public void testFolderCreation() {
        Assert.assertEquals(parserDir,replayService.getParserDirectory().getName());
        Assert.assertEquals(fileDir,replayService.getFileDirectory().getName());
    }

}
