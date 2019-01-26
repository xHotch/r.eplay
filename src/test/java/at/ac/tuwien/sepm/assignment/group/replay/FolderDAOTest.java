package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Philipp Hochhauser
 */
public class FolderDAOTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    /**
     * - @Mock annotation creates a mock object for the specified class or interface.
     * Simulates the definition of an object and populates it.
     */
    private FolderDAO folderDAO;
    private String parserDir;
    private String fileDir;
    private String heatmapDir;
    private File replayFile;
    private MatchDTO matchDTO;

    @Before
    public void setUp() throws CouldNotCreateFolderException, IOException {
        parserDir = "testParserDir";
        fileDir = "testFileDir";
        heatmapDir = "heatMapDir";
        folderDAO = new UserFolderDAO(parserDir, fileDir, heatmapDir);
        replayFile = new File(getClass().getResource("/testReplays/test.replay").getFile());

        matchDTO = new MatchDTO();
        MatchPlayerDTO matchplayer = new MatchPlayerDTO();
        matchplayer.setHeatmapFilename("testPlayerHeatmap.png");
        matchDTO.setBallHeatmapFilename("testBallHeatmap.png");

        List<MatchPlayerDTO> playerData = new LinkedList<>();
        playerData.add(matchplayer);
        matchDTO.setPlayerData(playerData);

        BufferedImage ballHeatmap = (ImageIO.read(new File(getClass().getResource("/images/testBallHeatmap.png").getFile())));
        BufferedImage playerHeatmap = (ImageIO.read(new File(getClass().getResource("/images/testPlayerHeatmap.png").getFile())));
        ImageIO.write(ballHeatmap, "png", new File(folderDAO.getHeatmapDirectory(), "testBallHeatmap.png"));
        ImageIO.write(playerHeatmap, "png", new File(folderDAO.getHeatmapDirectory(), "testPlayerHeatmap.png"));


    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(folderDAO.getFileDirectory());
        FileUtils.deleteDirectory(folderDAO.getParserDirectory());
        FileUtils.deleteDirectory(folderDAO.getHeatmapDirectory());
    }


    @Test
    public void testFolderCreation() {
        Assert.assertEquals(parserDir, folderDAO.getParserDirectory().getName());
        Assert.assertEquals(fileDir, folderDAO.getFileDirectory().getName());
        Assert.assertEquals(heatmapDir, folderDAO.getHeatmapDirectory().getName());
    }

    @Test
    public void testJsonCreation() {
        File jsonFile = folderDAO.createJsonFile(replayFile);

        Assert.assertEquals(FilenameUtils.getBaseName(replayFile.getName()), FilenameUtils.getBaseName(jsonFile.getName()));
        Assert.assertEquals(jsonFile.getParent(), folderDAO.getFileDirectory().getAbsolutePath());
    }

    @Test
    public void testCopyReplayFileAndDelete() throws FilePersistenceException {
        File copiedFile = folderDAO.copyReplayFile(replayFile);


        Assert.assertTrue(copiedFile.exists());
        Assert.assertEquals(copiedFile.getParent(), folderDAO.getFileDirectory().getAbsolutePath());

        folderDAO.deleteFile(copiedFile);
    }

    @Test(expected = FilePersistenceException.class)
    public void copyWrongFileFormat() throws FilePersistenceException {
        File randomExeFile = new File("test.exe");

        folderDAO.copyReplayFile(randomExeFile);
    }

    @Test(expected = FilePersistenceException.class)
    public void testFileDeletionWrongFiletype() throws FilePersistenceException {
        File randomExeFile = new File("test.exe");

        folderDAO.deleteFile(randomExeFile);
    }

    @Test(expected = FilePersistenceException.class)
    public void testFileDeletionEmptyFile() throws FilePersistenceException {
        File randomJsonFile = new File("test.json");

        folderDAO.deleteFile(randomJsonFile);
    }

    @Test
    public void getAndSaveHeatmapsShouldPersist() throws FilePersistenceException {
        folderDAO.getHeatmaps(matchDTO);
        Assert.assertNotNull(matchDTO.getBallHeatmapImage());
        Assert.assertNotNull(matchDTO.getPlayerData().get(0).getHeatmapImage());
    }

    @Test(expected = CouldNotCreateFolderException.class)
    public void testCouldNotCreateFolder() throws CouldNotCreateFolderException {
        folderDAO.setupDirectory(" /*!");
    }
}
