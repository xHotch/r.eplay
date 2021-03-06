package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FilterValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.ReplayAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleMatchService;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
@Ignore
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
        matchDTO.setReplayFilename("TestFile");
        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();
        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();
        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   7,TeamSide.RED, 10, 2,3, 5,1);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 8,TeamSide.BLUE, 15, 4,2, 3, 7);

        PreparedStatement ps = jdbcConnectionManager.getConnection().prepareStatement("INSERT INTO player SET id = ?, name = ?, plattformid = ?, shown = ?");
        ps.setInt(1,7);
        ps.setString(2,"Player red");
        ps.setInt(3,345456);
        ps.setBoolean(4,true);
        ps.executeUpdate();
        ps.setInt(1,8);
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

        matchDTO.setMatchTime(400);

        try {
            matchDAO.createMatch(matchDTO);
        } catch (MatchAlreadyExistsException e)
        {
            fail();
        }
        matchDAO.createMatch(matchDTO);
    }

    @Test
    public void matchCreateAndReadTest() throws MatchPersistenceException, SQLException, MatchAlreadyExistsException {
        // set up a match entity and define the object variables
        matchDTO = new MatchDTO();
        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());
        // set fileName
        matchDTO.setReplayFilename("TestFile");
        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();
        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();
        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();
        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   7,TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 8,TeamSide.BLUE, 15, 4,2, 3, 7);

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
        matchDTO.setMatchTime(400);

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
        Assert.assertThat(match.getMatchTime(), is(matchDTO.getMatchTime()));

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
        match.setMatchTime(-1);
        match.setPlayerData(null);

        try {
            matchService.createMatch(match);
            fail();
        } catch (MatchValidationException e) {
            //assertThat(e.getMessage(), CoreMatchers.is("No MatchDate\n" + "MatchTime cannot be negative\n" + "No players found in match\n"));
            assertThat(e.getMessage(), CoreMatchers.is("Kein Matchdatum\n" + "Matchdauer kann nicht negativ sein\n" + "Keine Spieler im Match gefunden\n"));
        }

        match.setDateTime(LocalDateTime.now());
        match.setMatchTime(400);
        match.setTeamSize(3);

        MatchPlayerDTO playerRed = new MatchPlayerDTO();
        MatchPlayerDTO playerBlue = new MatchPlayerDTO();

        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        setPlayerVariables(playerRed,match,playerR,"",7,TeamSide.BLUE,-1,-1,-1,-1,-1);
        setPlayerVariables(playerBlue,match, playerB,"",8,TeamSide.BLUE,-1,-1,-1,-1,-1);

        List<MatchPlayerDTO> players = new LinkedList<>();
        players.add(playerBlue);
        players.add(playerRed);

        match.setPlayerData(players);

        try {
            matchService.createMatch(match);
            fail();
        } catch (MatchValidationException e) {
            /*
            assertThat(e.getMessage(), CoreMatchers.is("Team size does not equal player list\n" +
                "No Name\n" + "Goals negativ\n" + "Shots negativ\n" + "Assists negativ\n" + "Saves negativ\n" + "Score negativ\n" +
                "No Name\n" + "Goals negativ\n" + "Shots negativ\n" + "Assists negativ\n" + "Saves negativ\n" + "Score negativ\n" +
                "Uneven teamsize\n"));
            */
            assertThat(e.getMessage(), CoreMatchers.is("Teamgröße stimmt nicht mit Spielerliste überein\n" +
                "Kein Name\n" + "Tore negativ\n" + "Schüsse negativ\n" + "Vorlagen negativ\n" + "Paraden negativ\n" + "Punkte negativ\n" +
                "Kein Name\n" + "Tore negativ\n" + "Schüsse negativ\n" + "Vorlagen negativ\n" + "Paraden negativ\n" + "Punkte negativ\n" +
                "Ungerade Teamgröße\n"));
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
        matchDTO.setReplayFilename("TestFile");

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   7,TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 8,TeamSide.BLUE, 15, 4,2, 3, 7);

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

        matchDTO.setMatchTime(400);

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
        matchDTO.setReplayFilename("TestFile");

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   7,TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 8,TeamSide.BLUE, 15, 4,2, 3, 7);

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

        matchDTO.setMatchTime(400);

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

    @Test
    public void calcTeamStatsTest() {
        MatchDTO match = new MatchDTO();
        MatchPlayerDTO player1 = new MatchPlayerDTO();
        MatchPlayerDTO player2 = new MatchPlayerDTO();
        MatchPlayerDTO player3 = new MatchPlayerDTO();
        MatchPlayerDTO player4 = new MatchPlayerDTO();
        player1.setTeam(TeamSide.RED);
        player2.setTeam(TeamSide.RED);
        player3.setTeam(TeamSide.BLUE);
        player4.setTeam(TeamSide.BLUE);
        player1.setGoals(1);
        player1.setShots(2);
        player1.setScore(220);
        player1.setAssists(0);
        player1.setSaves(0);
        player1.setAverageSpeed(1300);

        player2.setGoals(0);
        player2.setShots(1);
        player2.setScore(120);
        player2.setAssists(1);
        player2.setSaves(1);
        player2.setAverageSpeed(1400);

        List<MatchPlayerDTO> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);
        players.add(player4);
        match.setPlayerData(players);

        MatchStatsDTO result = matchService.calcTeamStats(match, TeamSide.RED);

        assertThat(result.getAssists(), is(1));
        assertThat(result.getGoals(), is(1));
        assertThat(result.getShots(), is(3));
        assertThat(result.getScore(), is(340));
        assertThat(result.getSaves(), is(1));
        assertThat(result.getAverageSpeed(), is(1350.0));
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

        //set boost pad list
        Map<Integer, List<BoostPadDTO>> boostPadMap = new HashMap<>();
        for(int i=0; i<=33; i++) {
            boostPadMap.putIfAbsent(i, new LinkedList<>());
            boostPadMap.get(i).add(new BoostPadDTO(0.0, 0.0, 0, false));
        }
        player.setBoostPadMap(boostPadMap);
    }
}
