package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimplePlayerService;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Gabriel Aichinger
 */
@RunWith(MockitoJUnitRunner.class)
public class PlayerTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private JDBCConnectionManager jdbcConnectionManager;
    private PlayerDAO playerDAO;
    private PlayerDTO player1, player2;
    private MatchPlayerDTO matchPlayer1;
    private MatchDAO matchDAO;
    private FolderDAO mockFolderDAO;
    private PlayerService playerService;
    private List<PlayerDTO> retrievedPlayers;

    @Before
    public void setUp() throws SQLException, CouldNotCreateFolderException {

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();


        playerDAO = new JDBCPlayerDAO(jdbcConnectionManager);
        mockFolderDAO = new UserFolderDAO("mockParser", "mockFiles", "mockHeatmap");
        matchDAO = new JDBCMatchDAO(jdbcConnectionManager, playerDAO, mockFolderDAO);



        //create players
        player1 = new PlayerDTO();
        player2 = new PlayerDTO();

        matchPlayer1 = new MatchPlayerDTO();

        // will be used as container for the results from the db.
        retrievedPlayers = new LinkedList<>();

        playerService = new SimplePlayerService(playerDAO, matchDAO);
    }

    @After
    public void tearDown() {

        // drop the table after the test runs
        Connection connection;
        try {

            connection = jdbcConnectionManager.getConnection();
            // drop all the tables for clean tests
            PreparedStatement dropMatchPlayer = connection.prepareStatement("DROP TABLE IF EXISTS MATCHPLAYER");
            PreparedStatement dropPlayer = connection.prepareStatement("DROP TABLE IF EXISTS PLAYER");

            // execute
            dropMatchPlayer.execute();
            dropPlayer.execute();

        } catch (SQLException e) {
                LOG.error("SQLException caught");
        }
        try {
            FileUtils.deleteDirectory(mockFolderDAO.getFileDirectory());
            FileUtils.deleteDirectory(mockFolderDAO.getParserDirectory());
            FileUtils.deleteDirectory(mockFolderDAO.getHeatmapDirectory());
        } catch (IOException e) {
            LOG.error("Exception while tearing Down Replay Service test", e);
        }

    }

    /**
     * This test saves two valid players. It checks the creation an retrievement of players.
     * The DAO should save it without an error.
     **/
    @Test
    public void createWithValidParametersShouldPersist() throws PlayerPersistenceException, PlayerServiceException, PlayerValidationException {
        player1.setName("Player 1");
        player2.setName("Player 2");
        //set with steamId length
        player1.setPlatformID(12345678910111213L);
        //set with psnId length
        player2.setPlatformID(1234567891011121314L);

        //setShown to true so the readPlayers method gets the retrieves the players afterwards
        player1.setShown(true);
        player2.setShown(true);

        playerService.createPlayer(player1);
        playerService.createPlayer(player2);

        // get all players
        retrievedPlayers = playerDAO.readPlayers();

        //check if players were saved correctly
        Assert.assertTrue(retrievedPlayers.contains(player1));
        Assert.assertTrue(retrievedPlayers.contains(player2));
    }

    /**
     * This test tries to save a player with a negative id. A PlayerValidationException should be thrown
     **/
    @Test(expected = PlayerValidationException.class)
    public void createWithNegativeShouldThrowException() throws PlayerServiceException, PlayerValidationException {

        player1.setName("Player 1");
        player1.setPlatformID(-1234);

        playerService.createPlayer(player1);
    }

    /**
     * This test tries to save a player with an empty name. A PlayerValidationException should be thrown
     **/
    @Test(expected = PlayerValidationException.class)
    public void createWithEmptyNameShouldThrowException() throws PlayerServiceException, PlayerValidationException {

        player1.setName("");
        player1.setPlatformID(123456789);

        playerService.createPlayer(player1);
    }

    /**
     * This test tries to delete a player. The DAO should delete it without an error.
     **/
    @Test
    public void deletePlayerShouldPersist() throws PlayerPersistenceException, PlayerServiceException, PlayerValidationException {
        player1.setName("Player 1");
        player1.setPlatformID(123456789101112L);
        player1.setShown(true);

        playerService.createPlayer(player1);

        // get all players
        retrievedPlayers = playerDAO.readPlayers();

        //check if player was saved correctly
        Assert.assertTrue(retrievedPlayers.contains(player1));

        List<PlayerDTO> playersToDelete = new LinkedList<>();
        playersToDelete.add(player1);

        playerService.deletePlayers(playersToDelete);

        //retrieve players again
        retrievedPlayers = playerDAO.readPlayers();

        //check if player was deleted correctly
        Assert.assertFalse(retrievedPlayers.contains(player1));
    }

    /**
     * This test tries to show a player.
     **/
    @Test
    public void showPlayerShouldPersist() throws PlayerPersistenceException, PlayerServiceException, PlayerValidationException {

        //set values for playerDTO
        player1.setName("Player 1");
        player1.setPlatformID(123456789101112L);
        player1.setShown(false);

        playerService.createPlayer(player1);

        //retrieve player
        retrievedPlayers = playerDAO.readPlayers();

        //check that player is not shown
        Assert.assertFalse(retrievedPlayers.contains(player1));

        //show player
        playerService.showPlayer(player1);

        //retrieve players again
        retrievedPlayers = playerDAO.readPlayers();

        //check if player is shown
        Assert.assertTrue(retrievedPlayers.contains(player1));
    }

    /**
     * This test checks if the wins and losses are calculated correct
     *
     */
    @Test
    public void getAvgStatsServiceTest() throws Exception {
        PlayerDAO playerDAO = mock(PlayerDAO.class);
        MatchDAO matchDAO = mock(MatchDAO.class);

        MatchType matchType = MatchType.RANKED1V1;
        PlayerDTO playerDTO = new PlayerDTO();
        playerDTO.setId(2);
        playerDTO.setName("Test");
        playerDTO.setPlatformID(12345);

        PlayerDTO playerDTO2 = new PlayerDTO();
        playerDTO2.setId(1);
        playerDTO2.setName("Test2");
        playerDTO2.setPlatformID(123456);

        AvgStatsDTO mockedStats = new AvgStatsDTO();
        mockedStats.setScore(220.0);
        mockedStats.setSpeed(1300.0);
        mockedStats.setShots(3.0);
        mockedStats.setSaves(1.0);
        mockedStats.setAssists(0.0);
        mockedStats.setGoals(2);



        MatchPlayerDTO player1 = new MatchPlayerDTO();
        player1.setAverageSpeed(1300.0);
        player1.setSaves(1);
        player1.setAssists(0);
        player1.setGoals(2);
        player1.setShots(3);
        player1.setScore(220);
        player1.setPlayerDTO(playerDTO);
        player1.setTeam(TeamSide.RED);

        MatchPlayerDTO player2 = new MatchPlayerDTO();
        player2.setAverageSpeed(1300.0);
        player2.setSaves(1);
        player2.setAssists(0);
        player2.setGoals(3);
        player2.setShots(3);
        player2.setScore(220);
        player2.setPlayerDTO(playerDTO2);
        player2.setTeam(TeamSide.BLUE);

        List<MatchPlayerDTO> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);


        MatchDTO match1 = new MatchDTO();

        match1.setTeamSize(1);
        match1.setId(12);
        match1.setPlayerData(players);

        List<MatchDTO> mockedMatchList = new ArrayList<>();
        mockedMatchList.add(match1);
        when(playerDAO.getAvgStats(playerDTO, matchType)).thenReturn(mockedStats);
        when(matchDAO.readMatchesFromPlayer(playerDTO)).thenReturn(mockedMatchList);
        PlayerService ps = new SimplePlayerService(playerDAO, matchDAO);

        AvgStatsDTO result = ps.getAvgStats(playerDTO, matchType);
        Assert.assertThat(result.getWins(), is(0));
        Assert.assertThat(result.getLosses(), is(1));
    }



}