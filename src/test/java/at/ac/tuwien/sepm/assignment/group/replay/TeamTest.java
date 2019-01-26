package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCPlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCTeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimplePlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleTeamService;
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
import java.util.LinkedList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TeamTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private JDBCConnectionManager jdbcConnectionManager;

    private FolderDAO folderDAO;
    private TeamDAO teamDAO;
    private TeamService teamService;
    private List<TeamDTO> retrievedTeams;
    private TeamDTO validTeamDTO1,validTeamDTO2;
    private LinkedList<PlayerDTO> playerData1,playerData2;


    @Before
    public void setUp() throws CouldNotCreateFolderException, SQLException, PlayerServiceException, PlayerValidationException {

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();

        PlayerDAO playerDAO = new JDBCPlayerDAO(jdbcConnectionManager);
        folderDAO = new UserFolderDAO("testParserDir", "testFileDir", "testHeatmapDir");
        JDBCMatchDAO matchDAO = new JDBCMatchDAO(jdbcConnectionManager, playerDAO, folderDAO);
        teamDAO = new JDBCTeamDAO(jdbcConnectionManager, playerDAO, matchDAO);

        teamService = new SimpleTeamService(teamDAO);
        PlayerService playerService = new SimplePlayerService(playerDAO,matchDAO);
        retrievedTeams = new LinkedList<>();

        //create two valid players
        PlayerDTO validPlayerDTO1, validPlayerDTO2;
        validPlayerDTO1 = new PlayerDTO();
        validPlayerDTO1.setName("Player 1");
        validPlayerDTO1.setPlatformID(12345678910111213L);

        validPlayerDTO2 = new PlayerDTO();
        validPlayerDTO2.setName("Player 2");
        validPlayerDTO2.setPlatformID(12345678910119999L);

        //create two valid teams
        validTeamDTO1 = new TeamDTO();
        validTeamDTO1.setName("Team 1");
        validTeamDTO1.setTeamSize(1);

        validTeamDTO2 = new TeamDTO();
        validTeamDTO2.setName("Team 2");
        validTeamDTO2.setTeamSize(1);

        //create player lists and add Player 1 to Team 1 and Player 2 to Team 2

        playerService.createPlayer(validPlayerDTO1);
        playerData1 = new LinkedList<>();
        playerData1.add(validPlayerDTO1);
        validTeamDTO1.setPlayers(playerData1);

        playerService.createPlayer(validPlayerDTO2);
        playerData2 = new LinkedList<>();
        playerData2.add(validPlayerDTO2);
        validTeamDTO2.setPlayers(playerData2);
    }

    @After
    public void tearDown() throws SQLException {

        // drop the table after the test runs
        Connection connection = null;

            // get the connection manager component
            connection = jdbcConnectionManager.getConnection();

            // drop all the tables for clean tests
            PreparedStatement dropMatchPlayer = connection.prepareStatement("DROP TABLE IF EXISTS MATCHPLAYER");
            PreparedStatement dropMatch = connection.prepareStatement("DROP TABLE IF EXISTS MATCH_");
            PreparedStatement dropTeam = connection.prepareStatement("DROP TABLE IF EXISTS TEAM");
            PreparedStatement dropTeamPlayer = connection.prepareStatement("DROP TABLE IF EXISTS TEAMPLAYER");
            PreparedStatement dropPlayer = connection.prepareStatement("DROP TABLE IF EXISTS PLAYER");

            // execute
            dropMatchPlayer.execute();
            dropMatch.execute();
            dropTeam.execute();
            dropTeamPlayer.execute();
            dropPlayer.execute();

        try {
            FileUtils.deleteDirectory(folderDAO.getFileDirectory());
            FileUtils.deleteDirectory(folderDAO.getParserDirectory());
            FileUtils.deleteDirectory(folderDAO.getHeatmapDirectory());
        } catch (IOException e) {
            LOG.error("Exception while tearing Down TeamDAO test", e);
        }
    }

    /**
     * This test saves a team with player. It checks the creation and retrievement.
     * The DAO should save it without an error.
     **/
    @Test
    public void createWithValidParametersShouldPersist() throws TeamServiceException, TeamValidationException, TeamPersistenceException {
        teamService.createTeam(validTeamDTO1);

        // get all teams
        retrievedTeams = teamDAO.readTeams();

        //check if team was saved correctly
        Assert.assertEquals(validTeamDTO1.getName(),retrievedTeams.get(0).getName());
    }

    /**
     * This test tries to create a team without a name.
     * It should throw a TeamValidationException
     **/
    @Test (expected = TeamValidationException.class)
    public void createWithoutNameShouldThrowTeamValidationException () throws TeamServiceException, TeamValidationException {
        TeamDTO invalidTeam = new TeamDTO();
        invalidTeam.setTeamSize(1);
        invalidTeam.setPlayers(playerData1);

        teamService.createTeam(invalidTeam);

    }

    /**
     * This test successfully creates a team and deletes it.
     **/
    @Test
    public void createAndDeleteShouldPersist() throws TeamServiceException, TeamValidationException, TeamPersistenceException {
        teamService.createTeam(validTeamDTO1);

        // get all teams
        retrievedTeams = teamDAO.readTeams();

        //check if team was saved correctly
        Assert.assertEquals(validTeamDTO1.getName(),retrievedTeams.get(0).getName());
        Assert.assertTrue(retrievedTeams.contains(validTeamDTO1));

        //delete team
        teamService.deleteTeam(validTeamDTO1);
        retrievedTeams = teamDAO.readTeams();
        Assert.assertFalse(retrievedTeams.contains(validTeamDTO1));
    }

    /**
     * This test tries to read matches of two teams. No matches should be found because no matches with the two teams
     * where added yet.
     */
    @Test
    public void readMatchesShouldReturnEmptyList() throws TeamPersistenceException {
        teamDAO.createTeam(validTeamDTO1);
        teamDAO.createTeam(validTeamDTO2);
        Assert.assertTrue(teamDAO.readTeamMatches(validTeamDTO1, validTeamDTO2).isEmpty());
    }
}
