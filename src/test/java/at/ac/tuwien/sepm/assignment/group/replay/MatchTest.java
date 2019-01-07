package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FilterValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.ReplayAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.ReplayServiceRLRP;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleMatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser.JsonParseServiceJsonPath;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.h2.jdbc.JdbcSQLException;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Brief description of the test runs...
 * - create a match DAO
 *      - call the createMatch() method.
 *      - verify if an actual match item exists in the DB after the call.
 *      - verify if all players exist in the DB from that match.
 *      - verify if the id's match with player and match.
 */
@RunWith(MockitoJUnitRunner.class)
public class MatchTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private JDBCConnectionManager jdbcConnectionManager;

    private MatchDAO matchDAO;

    private FolderDAO folderDAO;
    private PlayerDAO playerDAO;


    private MatchDTO matchDTO;

    private MatchService matchService;

    private MatchPlayerDTO playerRED, playerBLUE;

    private List<MatchDTO> retrievedMatches;



    @Before
    public void setUp() throws CouldNotCreateFolderException, SQLException{

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();


        playerDAO = new JDBCPlayerDAO(jdbcConnectionManager);

        // create UserFolderDAO
        folderDAO = new UserFolderDAO("testParserDir", "testFileDir", "testHeatmapDir");

        matchDAO = new JDBCMatchDAO(jdbcConnectionManager, playerDAO, folderDAO);

        matchService = new SimpleMatchService(matchDAO, folderDAO);


    }

    @After
    public void tearDown() {

        // drop the table after the test runs
        Connection connection = null;
        try {

            // get the connection manager component
            connection = jdbcConnectionManager.getConnection();
            // drop all the tables for clean tests
            PreparedStatement dropPlayerInMatch = connection.prepareStatement("DROP TABLE IF EXISTS PLAYERINMATCH");
            PreparedStatement dropMatchPlayer = connection.prepareStatement("DROP TABLE IF EXISTS MATCHPLAYER");
            PreparedStatement dropMatch = connection.prepareStatement("DROP TABLE IF EXISTS MATCH_");

            // execute
            dropPlayerInMatch.execute();
            dropMatchPlayer.execute();
            dropMatch.execute();

        } catch (SQLException e) {

        }



        try {
            FileUtils.deleteDirectory(folderDAO.getFileDirectory());
            FileUtils.deleteDirectory(folderDAO.getParserDirectory());
            FileUtils.deleteDirectory(folderDAO.getHeatmapDirectory());
        } catch (IOException e) {
            LOG.error("Exception while tearing Down Replay Service test", e);
        }

    }

    @Test(expected = MatchAlreadyExistsException.class)
    public void sameMatchTest() throws SQLException, MatchPersistenceException, MatchAlreadyExistsException {
        // set up a match entity and define the object variables
        matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // set fileName
        matchDTO.setReplayFile(new File("TestFile"));

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();


        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   1,TeamSide.RED, 10, 2,3, 5,1);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 2,TeamSide.BLUE, 15, 4,2, 3, 7);


        PreparedStatement ps = jdbcConnectionManager.getConnection().prepareStatement("INSERT INTO player SET id = ?, name = ?, plattformid = ?, shown = ?");
        ps.setInt(1,1);
        ps.setString(2,"Player red");
        ps.setInt(3,345456);
        ps.setBoolean(4,true);
        ps.executeUpdate();
        ps.setInt(1,2);
        ps.setString(2,"Player blue");
        ps.setInt(3,345333);
        ps.setBoolean(4,true);
        ps.executeUpdate();
        if (!ps.isClosed()) ps.close();



        playerMatchList.add(playerRED);
        playerMatchList.add(playerBLUE);
        matchDTO.setPlayerData(playerMatchList);
        matchDTO.setBallHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));

        // set the remaining match variables
        matchDTO.setTeamSize(1);

        matchDTO.setReadId("Test");

        matchDAO.createMatch(matchDTO);
        matchDAO.createMatch(matchDTO);
    }

    @Test
    public void matchCreateAndReadTest() throws MatchPersistenceException, SQLException, MatchAlreadyExistsException {
        // set up a match entity and define the object variables
        matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // set fileName
        matchDTO.setReplayFile(new File("TestFile"));

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   1,TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 2,TeamSide.BLUE, 15, 4,2, 3, 7);


        PreparedStatement ps = jdbcConnectionManager.getConnection().prepareStatement("INSERT INTO player SET id = ?, name = ?, plattformid = ?, shown = ?");
        ps.setLong(1,playerB.getId());
        ps.setString(2,playerB.getName());
        ps.setInt(3,345456);
        ps.setBoolean(4,true);

        ps.executeUpdate();
        ps.setLong(1,playerR.getId());
        ps.setString(2,playerR.getName());
        ps.setInt(3,345333);
        ps.setBoolean(4,true);

        ps.executeUpdate();

        if (!ps.isClosed()) ps.close();

        playerMatchList.add(playerRED);
        playerMatchList.add(playerBLUE);
        matchDTO.setPlayerData(playerMatchList);
        matchDTO.setBallHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));

        // set the remaining match variables
        matchDTO.setTeamSize(1);

        matchDTO.setReadId("Test");

        // will be used as container for the results from the db.
        retrievedMatches = new LinkedList<>();

        // create the match in the database
        matchDAO.createMatch(matchDTO);

        // retrieve match from the database
        retrievedMatches = matchDAO.readMatches();

        // check if the received database entries match the stuff in the setUp() method.
        // not using a loop here because only one match was inserted before.
        MatchDTO match = retrievedMatches.get(0);

        // assert things
        Assert.assertThat(match.getId(), is(1));
        Assert.assertThat(match.getDateTime(), is(matchDTO.getDateTime()));
        Assert.assertThat(match.getTeamSize(), is(matchDTO.getTeamSize()));
        Assert.assertThat(match.getReadId(), is(matchDTO.getReadId()));

        // verify player data ...
        for (MatchPlayerDTO player:match.getPlayerData()) {
            MatchPlayerDTO compare;
            if (player.getTeam() == TeamSide.RED) compare = playerRED;
            else compare = playerBLUE;

            Assert.assertThat(player.getName(), is(compare.getName()));
            Assert.assertThat(player.getTeam(), is(compare.getTeam()));
            Assert.assertThat(player.getAssists(), is(compare.getAssists()));
            Assert.assertThat(player.getGoals(), is(compare.getGoals()));
            Assert.assertThat(player.getSaves(), is(compare.getSaves()));
            Assert.assertThat(player.getScore(), is(compare.getScore()));
            Assert.assertThat(player.getShots(), is(compare.getShots()));
        }
    }

    @Test
    public void validationMatchTest() throws MatchServiceException, ReplayAlreadyExistsException {
        MatchDTO match = new MatchDTO();

        match.setDateTime(null);
        match.setPlayerData(null);

        try {
            matchService.createMatch(match);
            fail();
        } catch (MatchValidationException e) {
            assertThat(e.getMessage(), CoreMatchers.is("No MatchDate\n" + "No players found in match\n"));
        }

        match.setDateTime(LocalDateTime.now());
        match.setTeamSize(3);

        MatchPlayerDTO playerRed = new MatchPlayerDTO();
        MatchPlayerDTO playerBlue = new MatchPlayerDTO();

        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        setPlayerVariables(playerRed,match,playerR,"",1,TeamSide.BLUE,-1,-1,-1,-1,-1);
        setPlayerVariables(playerBlue,match, playerB,"",2,TeamSide.BLUE,-1,-1,-1,-1,-1);

        List<MatchPlayerDTO> players = new LinkedList<>();
        players.add(playerBlue);
        players.add(playerRed);

        match.setPlayerData(players);

        try {
            matchService.createMatch(match);
            fail();
        } catch (MatchValidationException e) {
            assertThat(e.getMessage(), CoreMatchers.is("Team size does not equal player list\n" +
                "No Name\n" + "Goals negativ\n" + "Shots negativ\n" + "Assists negativ\n" + "Saves negativ\n" + "Score negativ\n" +
                "No Name\n" + "Goals negativ\n" + "Shots negativ\n" + "Assists negativ\n" + "Saves negativ\n" + "Score negativ\n" +
                "Uneven teamsize\n"));
        }
    }

    @Test(expected = FilterValidationException.class)
    public void validateSearchParamNegativeTeamSizeTest() throws FilterValidationException, MatchServiceException {
        matchService.searchMatches(null, null, null, -1);
    }

    @Test(expected = FilterValidationException.class)
    public void validateSearchParamBeginAfterEndTest() throws FilterValidationException, MatchServiceException {
        LocalDateTime dateTime = LocalDateTime.now();
        matchService.searchMatches(null, dateTime, dateTime.minusDays(1), 0);
    }

    @Test
    public void searchMatchTest() throws SQLException, MatchPersistenceException, MatchAlreadyExistsException {
        // set up a match entity and define the object variables
        matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // set fileName
        matchDTO.setReplayFile(new File("TestFile"));

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   1,TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 2,TeamSide.BLUE, 15, 4,2, 3, 7);


        PreparedStatement ps = jdbcConnectionManager.getConnection().prepareStatement("INSERT INTO player SET id = ?, name = ?, plattformid = ?, shown = ?");
        ps.setLong(1,playerB.getId());
        ps.setString(2,playerB.getName());
        ps.setInt(3,345456);
        ps.setBoolean(4,true);

        ps.executeUpdate();
        ps.setLong(1,playerR.getId());
        ps.setString(2,playerR.getName());
        ps.setInt(3,345333);
        ps.setBoolean(4,true);

        ps.executeUpdate();

        if (!ps.isClosed()) ps.close();

        playerMatchList.add(playerRED);
        playerMatchList.add(playerBLUE);
        matchDTO.setPlayerData(playerMatchList);
        matchDTO.setBallHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));

        // set the remaining match variables
        matchDTO.setTeamSize(1);

        matchDTO.setReadId("Test");

        // will be used as container for the results from the db.
        retrievedMatches = new LinkedList<>();

        // create the match in the database
        matchDAO.createMatch(matchDTO);

        retrievedMatches = matchDAO.searchMatches(null, null, null, 0);
        assertThat(retrievedMatches.size(), is(1));

        retrievedMatches = matchDAO.searchMatches("Player re", null, null, 0);
        assertThat(retrievedMatches.size(), is(1));

        retrievedMatches = matchDAO.searchMatches("xxx", null, null, 0);
        assertThat(retrievedMatches.size(), is(0));

        retrievedMatches = matchDAO.searchMatches(null, null, null, 1);
        assertThat(retrievedMatches.size(), is(1));

        retrievedMatches = matchDAO.searchMatches(null, null, null, 2);
        assertThat(retrievedMatches.size(), is(0));

        retrievedMatches = matchDAO.searchMatches(null, matchDTO.getDateTime().minusDays(1), matchDTO.getDateTime().plusDays(1), 0);
        assertThat(retrievedMatches.size(), is(1));

        retrievedMatches = matchDAO.searchMatches(null, matchDTO.getDateTime().plusDays(1), matchDTO.getDateTime().plusDays(2), 0);
        assertThat(retrievedMatches.size(), is(0));
    }

    @Test
    public void readMatchesFromPlayerTest() throws SQLException, MatchPersistenceException, MatchAlreadyExistsException {
        // set up a match entity and define the object variables
        matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // set fileName
        matchDTO.setReplayFile(new File("TestFile"));

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   1,TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 2,TeamSide.BLUE, 15, 4,2, 3, 7);


        PreparedStatement ps = jdbcConnectionManager.getConnection().prepareStatement("INSERT INTO player SET id = ?, name = ?, plattformid = ?, shown = ?");
        ps.setLong(1,playerB.getId());
        ps.setString(2,playerB.getName());
        ps.setInt(3,345456);
        ps.setBoolean(4,true);

        ps.executeUpdate();
        ps.setLong(1,playerR.getId());
        ps.setString(2,playerR.getName());
        ps.setInt(3,345333);
        ps.setBoolean(4,true);

        ps.executeUpdate();

        if (!ps.isClosed()) ps.close();

        playerMatchList.add(playerRED);
        playerMatchList.add(playerBLUE);
        matchDTO.setPlayerData(playerMatchList);
        matchDTO.setBallHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));

        // set the remaining match variables
        matchDTO.setTeamSize(1);

        matchDTO.setReadId("Test");

        // will be used as container for the results from the db.
        retrievedMatches = new LinkedList<>();

        // create the match in the database
        matchDAO.createMatch(matchDTO);

        retrievedMatches = matchDAO.readMatchesFromPlayer(playerB);
        assertThat(retrievedMatches.size(), is(1));

        PlayerDTO player = new PlayerDTO();
        player.setId(10);

        retrievedMatches = matchDAO.readMatchesFromPlayer(player);
        assertThat(retrievedMatches.size(), is(0));
    }


    // helper class to populate the player's variables
    public void setPlayerVariables(MatchPlayerDTO player, MatchDTO match, PlayerDTO playerDTO, String name, int id, TeamSide team, int score, int goals, int assists, int shots, int saves){
        playerDTO.setName(name);
        playerDTO.setId(id);

        player.setMatchDTO(match);
        player.setPlayerDTO(playerDTO);
        player.setTeam(team);
        player.setScore(score);
        player.setGoals(goals);
        player.setAssists(assists);
        player.setShots(shots);
        player.setSaves(saves);
        player.setHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));
    }
}
