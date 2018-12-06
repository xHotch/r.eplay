package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimplePlayerService;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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
    private PlayerService playerService;
    private List<PlayerDTO> retrievedPlayers;

    AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {

        // needed to get the spring context so all components work
        context = new AnnotationConfigApplicationContext("at.ac.tuwien.sepm.assignment.group");

        // get the PlayerDAO component from the spring framework
        playerDAO = (JDBCPlayerDAO) context.getBean("JDBCPlayerDAO");

        // get the MatchDAO component from the spring framework
        matchDAO = (JDBCMatchDAO) context.getBean("JDBCMatchDAO");



        //create players
        player1 = new PlayerDTO();
        player2 = new PlayerDTO();

        matchPlayer1 = new MatchPlayerDTO();

        // will be used as container for the results from the db.
        retrievedPlayers = new LinkedList<>();

        playerService = new SimplePlayerService(playerDAO);
    }

    @After
    public void tearDown() {

        // drop the table after the test runs
        Connection connection;
        try {

            // get the connection manager component
            jdbcConnectionManager = (JDBCConnectionManager) context.getBean("JDBCConnectionManager");
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

        // finally close the spring framework context
        if (context != null) {
            context.close();
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



}