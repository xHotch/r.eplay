package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.AvgStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;


public class PlayerDAOTest {

    private JDBCConnectionManager jdbcConnectionManager;
    private FolderDAO folderDAO;

    private PlayerDAO playerDAO;

    @Before
    public void setUp() throws CouldNotCreateFolderException, SQLException {

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();

        folderDAO = new UserFolderDAO("testParserDir", "testFileDir", "testHeatmapDir");
        playerDAO = new JDBCPlayerDAO(jdbcConnectionManager);
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
    public void readPlayersTest() throws PlayerPersistenceException {
        List<PlayerDTO> players = playerDAO.readPlayers();
        Assert.assertThat(players.size(), is(4));
        PlayerDTO player1 = players.get(0);
        Assert.assertThat(player1.getName(), is("Player 1"));
        Assert.assertThat(player1.getId(), is(1L));
        Assert.assertThat(player1.getPlatformID(), is(123456L));
        Assert.assertTrue(player1.isShown());
    }

    @Test(expected = PlayerPersistenceException.class)
    public void readPlayersWithoutConnectionShouldThrowException() throws PlayerPersistenceException {
        jdbcConnectionManager.closeConnection();
        playerDAO.readPlayers();
    }

    @Test
    public void createPlayerTest() throws PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setPlatformID(123L);
        player.setName("Test Player");
        player.setShown(true);

        playerDAO.createPlayer(player);

        List<PlayerDTO> players = playerDAO.readPlayers();

        PlayerDTO readPlayer = players.get(4);
        Assert.assertThat(readPlayer.getName(), is("Test Player"));
        Assert.assertThat(readPlayer.getId(), is(7L));
        Assert.assertThat(readPlayer.getPlatformID(), is(123L));
        Assert.assertTrue(readPlayer.isShown());
    }

    @Test(expected = PlayerPersistenceException.class)
    public void createPlayerWithoutConnectionShouldThrowException() throws PlayerPersistenceException {
        jdbcConnectionManager.closeConnection();
        PlayerDTO player = new PlayerDTO();
        player.setPlatformID(123L);
        player.setName("Test Player");
        player.setShown(true);
        playerDAO.createPlayer(player);
    }

    @Test
    public void deletePlayerTest() throws PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        playerDAO.deletePlayer(player);
        List<PlayerDTO> players = playerDAO.readPlayers();
        Assert.assertThat(players.size(), is(3));
    }

    @Test(expected = PlayerPersistenceException.class)
    public void deletePlayerWithoutConnectionShouldThrowException() throws PlayerPersistenceException {
        jdbcConnectionManager.closeConnection();
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        playerDAO.deletePlayer(player);
    }

    @Test
    public void showPlayerTest() throws PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(5L);
        playerDAO.showPlayer(player);
        List<PlayerDTO> players = playerDAO.readPlayers();
        Assert.assertThat(players.size(), is(5));
    }

    @Test(expected = PlayerPersistenceException.class)
    public void showPlayerWithoutConnectionShouldThrowException() throws PlayerPersistenceException {
        jdbcConnectionManager.closeConnection();
        PlayerDTO player = new PlayerDTO();
        player.setId(5L);
        playerDAO.showPlayer(player);
    }

    @Test
    public void getTest() throws PlayerPersistenceException {
        PlayerDTO player = playerDAO.get(1);
        Assert.assertThat(player.getName(), is("Player 1"));
        Assert.assertThat(player.getId(), is(1L));
        Assert.assertThat(player.getPlatformID(), is(123456L));
        Assert.assertTrue(player.isShown());
    }

    @Test(expected = PlayerPersistenceException.class)
    public void getWithoutConnectionShouldThrowException() throws PlayerPersistenceException {
        jdbcConnectionManager.closeConnection();
        playerDAO.get(1);
    }

    @Test
    public void getAvgStatsTest() throws PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        MatchType matchType = MatchType.RANKED2V2;
        AvgStatsDTO avgStatsDTO = playerDAO.getAvgStats(player, matchType);
        Assert.assertThat(avgStatsDTO.getAssists(), is(0.0));
        Assert.assertThat(avgStatsDTO.getSpeed(), is(1300.0));
        Assert.assertThat(avgStatsDTO.getScore(), is(200.0));
        Assert.assertThat(avgStatsDTO.getSaves(), is(0.0));
        Assert.assertThat(avgStatsDTO.getGoals(), is(2.0));
        Assert.assertThat(avgStatsDTO.getShots(), is(3.5));
    }

    @Test(expected = PlayerPersistenceException.class)
    public void getAvgStatsWithoutConnectionShouldThrowException() throws PlayerPersistenceException {
        jdbcConnectionManager.closeConnection();
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        MatchType matchType = MatchType.RANKED2V2;
        playerDAO.getAvgStats(player, matchType);
    }



}
