package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.MatchValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleMatchService;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

    private MatchDTO matchDTO;

    private MatchService matchService;

    private MatchPlayerDTO playerRED, playerBLUE;

    private List<MatchDTO> retrievedMatches;

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp(){

        // needed to get the spring context so all components work
        context = new AnnotationConfigApplicationContext("at.ac.tuwien.sepm.assignment.group");

        // get the MatchDAO component from the spring framework
        matchDAO = (JDBCMatchDAO)context.getBean("JDBCMatchDAO");

        matchService = new SimpleMatchService(matchDAO);

        jdbcConnectionManager = (JDBCConnectionManager) context.getBean("JDBCConnectionManager");
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

        // finally close the spring framework context
        if (context != null) {
            context.close();
        }

    }
    @Test(expected = MatchAlreadyExistsException.class)
    public void sameMatchTest() throws SQLException, MatchPersistenceException, MatchAlreadyExistsException {
        // set up a match entity and define the object variables
        matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();


        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   1,3, 10, 2,3, 5,1);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 2,1, 15, 4,2, 3, 7);

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

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        playerRED = new MatchPlayerDTO();
        playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   1,3,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 2,1, 15, 4,2, 3, 7);


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
            if (player.getTeam() == 3) compare = playerRED;
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
    public void validationMatchTest() throws MatchServiceException, MatchAlreadyExistsException {
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

        setPlayerVariables(playerRed,match,playerR,"",1,-1,-1,-1,-1,-1,-1);
        setPlayerVariables(playerBlue,match, playerB,"",2,-1,-1,-1,-1,-1,-1);

        List<MatchPlayerDTO> players = new LinkedList<>();
        players.add(playerBlue);
        players.add(playerRed);

        match.setPlayerData(players);

        try {
            matchService.createMatch(match);
            fail();
        } catch (MatchValidationException e) {
            assertThat(e.getMessage(), CoreMatchers.is("Team size does not equal player list\n" +
                "No Name\n" + "Invalid Team number\n" + "Goals negativ\n" + "Shots negativ\n" + "Assists negativ\n" + "Saves negativ\n" + "Score negativ\n" +
                "No Name\n" + "Invalid Team number\n" + "Goals negativ\n" + "Shots negativ\n" + "Assists negativ\n" + "Saves negativ\n" + "Score negativ\n" +
                "Uneven teamsize\n"));
        }
    }

    // helper class to populate the player's variables
    public void setPlayerVariables(MatchPlayerDTO player, MatchDTO match, PlayerDTO playerDTO, String name,int id,  int team, int score, int goals, int assists, int shots, int saves){
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
    }
}
