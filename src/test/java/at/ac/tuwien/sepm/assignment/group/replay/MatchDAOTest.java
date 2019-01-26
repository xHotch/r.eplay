package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MatchDAOTest {

    private JDBCConnectionManager jdbcConnectionManager;
    private FolderDAO folderDAO;
    private FolderDAO folderDAOMock;

    private MatchDAO matchDAO;


    private PlayerDAO playerDAO;

    @Before
    public void setUp() throws CouldNotCreateFolderException, SQLException, PlayerPersistenceException {

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();

        playerDAO = mock(PlayerDAO.class);
        PlayerDTO player1 = new PlayerDTO();
        player1.setId(1L);
        player1.setShown(true);
        player1.setName("Player 1");
        player1.setPlatformID(123456L);
        when(playerDAO.get(1)).thenReturn(player1);
        PlayerDTO player2 = new PlayerDTO();
        player2.setId(2L);
        player2.setShown(true);
        player2.setName("Player 2");
        player2.setPlatformID(1234567L);
        when(playerDAO.get(2)).thenReturn(player2);
        PlayerDTO player3 = new PlayerDTO();
        player3.setId(3L);
        player3.setShown(true);
        player3.setName("Player 3");
        player3.setPlatformID(12345678L);
        when(playerDAO.get(3)).thenReturn(player3);
        PlayerDTO player4 = new PlayerDTO();
        player4.setId(4L);
        player4.setShown(true);
        player4.setName("Player 4");
        player4.setPlatformID(123456789L);
        when(playerDAO.get(4)).thenReturn(player4);

        folderDAO = new UserFolderDAO("testParserDir", "testFileDir", "testHeatmapDir");

        folderDAOMock = mock(FolderDAO.class);
        matchDAO = new JDBCMatchDAO(jdbcConnectionManager, playerDAO, folderDAOMock);






    }

    @After
    public void tearDown() {
        Connection connection = null;
        try {

            // get the connection manager component
            connection = jdbcConnectionManager.getConnection();
            // drop all the tables for clean tests
            PreparedStatement dropMatchPlayerBoostPads = connection.prepareStatement("DROP TABLE IF EXISTS matchPlayerBoostPads");
            PreparedStatement dropMatchPlayer = connection.prepareStatement("DROP TABLE IF EXISTS MATCHPLAYER");
            PreparedStatement dropMatch = connection.prepareStatement("DROP TABLE IF EXISTS MATCH_");
            PreparedStatement dropTeamPlayer = connection.prepareStatement("DROP TABLE IF EXISTS TEAMPLAYER");
            PreparedStatement dropTeam = connection.prepareStatement("DROP TABLE IF EXISTS Team");
            PreparedStatement dropPlayer = connection.prepareStatement("DROP TABLE IF EXISTS Player");

            // execute
            dropMatchPlayerBoostPads.execute();
            dropMatchPlayer.execute();
            dropMatch.execute();
            dropTeamPlayer.execute();
            dropTeam.execute();
            dropPlayer.execute();

        } catch (SQLException e) {

        }



        try {
            FileUtils.deleteDirectory(folderDAO.getFileDirectory());
            FileUtils.deleteDirectory(folderDAO.getParserDirectory());
            FileUtils.deleteDirectory(folderDAO.getHeatmapDirectory());
        } catch (
            IOException e) {
            //LOG.error("Exception while tearing Down Replay Service test", e);
        }
    }

    @Test
    public void readMatchesTest() throws MatchPersistenceException {



        List<MatchDTO> matches = matchDAO.readMatches();
        Assert.assertThat(matches.size(), is(2));

        MatchDTO match = matches.get(0);

        Assert.assertThat(match.getId(), is(1));
        Assert.assertThat(match.getTeamSize(), is(2));
        Assert.assertThat(match.getDateTime(), is(LocalDateTime.of(2018, 12, 2, 0, 0, 0)));
        Assert.assertThat(match.getMatchTime(), is(350.0));
        Assert.assertThat(match.getReadId(), is("12345"));
        Assert.assertThat(match.getBallHeatmapFilename(), is("match1.png"));
        Assert.assertThat(match.getPossessionBlue(), is(60));
        Assert.assertThat(match.getPossessionRed(), is(40));
        Assert.assertThat(match.getTimeBallInBlueSide(), is(140.0));
        Assert.assertThat(match.getTimeBallInRedSide(), is(150.0));

        List<MatchPlayerDTO> matchPlayers = match.getPlayerData();
        Assert.assertThat(matchPlayers.size(), is(4));
        MatchPlayerDTO matchPlayerDTO = matchPlayers.get(0);

        Assert.assertThat(matchPlayerDTO.getPlayerId(), is(1L));
        Assert.assertThat(matchPlayerDTO.getMatchId(), is(1));
        Assert.assertThat(matchPlayerDTO.getName(), is("Player 1"));
        Assert.assertThat(matchPlayerDTO.getTeam(), is(TeamSide.BLUE));
        Assert.assertThat(matchPlayerDTO.getGoals(), is(1));
        Assert.assertThat(matchPlayerDTO.getAssists(), is(0));
        Assert.assertThat(matchPlayerDTO.getShots(), is(2));
        Assert.assertThat(matchPlayerDTO.getSaves(), is(0));
        Assert.assertThat(matchPlayerDTO.getScore(), is(100));
        Assert.assertThat(matchPlayerDTO.getAverageDistanceToBall(), is(2400.0));
        Assert.assertThat(matchPlayerDTO.getAirTime(), is(110.0));
        Assert.assertThat(matchPlayerDTO.getAverageSpeed(), is(1300.0));
        Assert.assertThat(matchPlayerDTO.getEnemySideTime(), is(95.0));
        Assert.assertThat(matchPlayerDTO.getGroundTime(), is(195.0));
        Assert.assertThat(matchPlayerDTO.getHeatmapFilename(), is("11.png"));
        Assert.assertThat(matchPlayerDTO.getHomeSideTime(), is(205.0));


    }

    @Test
    public void createMatchTest() throws MatchPersistenceException, MatchAlreadyExistsException {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setReadId("Test");
        matchDTO.setTeamSize(1);
        matchDTO.setDateTime(LocalDateTime.now());
        matchDTO.setMatchTime(123.0);
        matchDTO.setBallHeatmapFilename("Test.png");
        matchDTO.setPossessionBlue(50);
        matchDTO.setPossessionRed(50);
        matchDTO.setTimeBallInBlueSide(60.0);
        matchDTO.setTimeBallInRedSide(60.0);
        matchDTO.setReplayFilename("Test.replay");

        PlayerDTO player1 = new PlayerDTO();
        player1.setId(1L);
        player1.setName("Player 1");
        PlayerDTO player2 = new PlayerDTO();
        player2.setId(2L);
        player2.setName("Player 2");

        MatchPlayerDTO matchPlayer1 = new MatchPlayerDTO();
        matchPlayer1.setPlayerDTO(player1);

        MatchPlayerDTO matchPlayer2 = new MatchPlayerDTO();
        matchPlayer2.setPlayerDTO(player2);

        matchPlayer1.setTeam(TeamSide.BLUE);
        matchPlayer1.setScore(100);
        matchPlayer1.setGoals(1);
        matchPlayer1.setShots(1);
        matchPlayer1.setAverageSpeed(1300.0);
        matchPlayer1.setHeatmapFilename("1.png");
        Map<Integer, List<Integer>> boostMap = new HashMap<>();
        for(int i = 0; i < 34; i++) {
            boostMap.put(i, new ArrayList<>());
        }
        Map<Integer, List<BoostPadDTO>> boostPadMap = new HashMap<>();
        for(int i = 0; i < 34; i++) {
            boostPadMap.put(i, new ArrayList<>());
        }
        matchPlayer1.setBoostPadMap(boostPadMap);
        matchPlayer1.setDBBoostPadMap(boostMap);

        matchPlayer2.setTeam(TeamSide.RED);
        matchPlayer2.setAverageSpeed(1300.0);
        matchPlayer2.setHeatmapFilename("2.png");
        matchPlayer2.setDBBoostPadMap(boostMap);
        matchPlayer2.setBoostPadMap(boostPadMap);

        List<MatchPlayerDTO> matchPlayers = new ArrayList<>();
        matchPlayers.add(matchPlayer1);
        matchPlayers.add(matchPlayer2);

        matchDTO.setPlayerData(matchPlayers);

        matchDAO.createMatch(matchDTO);
        List<MatchDTO> matches = matchDAO.readMatches();
        Assert.assertThat(matches.size(), is(3));
        matchPlayers = matches.get(2).getPlayerData();
        Assert.assertThat(matchPlayers.size(), is(2));
    }

    @Test(expected = MatchAlreadyExistsException.class)
    public void createMatchTestShouldThrowMatchAlreadyExistsException() throws MatchPersistenceException, MatchAlreadyExistsException {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setReadId("12345");
        matchDAO.createMatch(matchDTO);
    }

    @Test
    public void deleteMatchTest() throws MatchPersistenceException {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(1);
        matchDAO.deleteMatch(matchDTO);
        Assert.assertThat(matchDAO.readMatches().size(), is(1));
    }

    @Test
    public void searchMatchesTest() throws MatchPersistenceException {
        List<MatchDTO> retrievedMatches = matchDAO.searchMatches("Play", null, null, 0);
        Assert.assertThat(retrievedMatches.size(), is(2));

        retrievedMatches = matchDAO.searchMatches(null, null, null, 0);
        Assert.assertThat(retrievedMatches.size(), is(2));

        retrievedMatches = matchDAO.searchMatches("xxx", null, null, 0);
        Assert.assertThat(retrievedMatches.size(), is(0));

        retrievedMatches = matchDAO.searchMatches(null, null, null, 1);
        Assert.assertThat(retrievedMatches.size(), is(0));

        retrievedMatches = matchDAO.searchMatches(null, null, null, 2);
        Assert.assertThat(retrievedMatches.size(), is(2));

        retrievedMatches = matchDAO.searchMatches(null, LocalDateTime.of(2018, 1, 1, 0,0,0), LocalDateTime.of(2018, 12, 31, 0,0,0), 0);
        Assert.assertThat(retrievedMatches.size(), is(2));

        retrievedMatches = matchDAO.searchMatches(null, LocalDateTime.of(2017, 1, 1, 0,0,0), LocalDateTime.of(2017, 12, 31, 0,0,0), 0);
        Assert.assertThat(retrievedMatches.size(), is(0));
    }

    @Test
    public void readMatchesFromPlayerTest() throws MatchPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(1);
        List<MatchDTO> retrievedMatches = matchDAO.readMatchesFromPlayer(player);
        Assert.assertThat(retrievedMatches.size(), is(2));

        player = new PlayerDTO();
        player.setId(5);

        retrievedMatches = matchDAO.readMatchesFromPlayer(player);
        Assert.assertThat(retrievedMatches.size(), is(0));
    }
}
