package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.ReplayServiceRLRP;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleMatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.BallStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.BoostStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.PlayerStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.RigidBodyStatistic;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class JsonParseServiceTest {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File jsonFile;
    private File badJsonFile;
    private File goodReplay;
    private JDBCConnectionManager jdbcConnectionManager;

    private JsonParseService jsonParseService;
    private ReplayService replayService;
    private MatchService matchService;
    private RigidBodyParser rigidBodyParser;
    private BallInformationParser ballInformationParser;
    private CarInformationParser carInformationParser;
    private BoostInformationParser boostInformationParser;
    private GameInformationParser gameInformationParser;
    private PlayerInformationParser playerInformationParser;

    private PlayerStatistic playerStatistic;
    private BallStatistic ballStatistic;
    private BoostStatistic boostStatistic;
    private RigidBodyStatistic rigidBodyStatistic;
    private FolderDAO folderDAO;
    private PlayerDAO playerDAO;
    private MatchDAO matchDAO;

    @Before
    public void setUp() throws CouldNotCreateFolderException, SQLException {

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();


        jsonFile = new File(getClass().getResource("/testJson/kurzesreplay.json").getFile());
        badJsonFile = new File(getClass().getResource("/testJson/keinreplay.json").getFile());
        goodReplay = new File(getClass().getResource("/testJson/goodReplay.json").getFile());

        playerDAO = new JDBCPlayerDAO(jdbcConnectionManager);
        folderDAO = new UserFolderDAO("mockParser", "mockFiles", "mockHeatmaps");
        matchDAO = new JDBCMatchDAO(jdbcConnectionManager, playerDAO, folderDAO);


        rigidBodyParser = new RigidBodyParser();
        replayService = new ReplayServiceRLRP(folderDAO);
        matchService = new SimpleMatchService(matchDAO, folderDAO);

        ballInformationParser = new BallInformationParser(rigidBodyParser);
        carInformationParser = new CarInformationParser(rigidBodyParser);
        boostInformationParser = new BoostInformationParser(carInformationParser);
        gameInformationParser = new GameInformationParser();
        playerInformationParser = new PlayerInformationParser();

        rigidBodyStatistic = new RigidBodyStatistic();
        ballStatistic = new BallStatistic(rigidBodyStatistic);
        playerStatistic = new PlayerStatistic(rigidBodyStatistic);
        boostStatistic = new BoostStatistic();

        jsonParseService = new JsonParseServiceJsonPath(rigidBodyParser,playerInformationParser,gameInformationParser,carInformationParser,ballInformationParser,boostInformationParser,playerStatistic,ballStatistic,boostStatistic,replayService, matchService, folderDAO);
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(folderDAO.getFileDirectory());
        FileUtils.deleteDirectory(folderDAO.getParserDirectory());
        FileUtils.deleteDirectory(folderDAO.getHeatmapDirectory());
    }

    @Test
    public void testParseMatchReturnsCorrectMatchDto() throws FileServiceException {
        MatchDTO match = jsonParseService.parseMatch(goodReplay);
        Assert.assertThat(match.getTeamSize(), is(3));
        Assert.assertThat(match.getReadId(), is("2F2200D4435F2EF5691E298320832A4B"));
        Assert.assertThat(match.getDateTime(), is(LocalDateTime.of(2018, 12, 2, 17, 7, 28)));

        List<MatchPlayerDTO> matchPlayerDTOS = match.getPlayerData();

        boolean playerFound = false;
        for (MatchPlayerDTO matchPlayerDTO : matchPlayerDTOS) {
            if (matchPlayerDTO.getPlayerDTO().getPlatformID() == (3533021571419362446L)) {
                playerFound = true;
                Assert.assertThat(matchPlayerDTO.getGoals(), is(1));
            }
        }
        Assert.assertThat(playerFound, is(true));
    }

    @Test(expected = FileServiceException.class)
    public void testParseMatchThrowsExceptionWhenUsedWithBadJson() throws FileServiceException {
        jsonParseService.parseMatch(badJsonFile);
    }





}
