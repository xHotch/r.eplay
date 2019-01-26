package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCMatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.JDBCTeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TeamDAOTest {

    private JDBCConnectionManager jdbcConnectionManager;
    private FolderDAO folderDAO;

    private TeamDAO teamDAO;
    private PlayerDAO playerDAO;
    private JDBCMatchDAO jdbcMatchDAO;

    @Before
    public void setUp() throws SQLException, PlayerPersistenceException, CouldNotCreateFolderException {

        jdbcConnectionManager = MockDatabase.getJDBCConnectionManager();

        folderDAO = new UserFolderDAO();

        playerDAO = mock(PlayerDAO.class);
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        player.setName("Player 1");
        player.setPlatformID(123456);
        player.setShown(true);
        when(playerDAO.get(1)).thenReturn(player);

        player = new PlayerDTO();
        player.setId(2L);
        player.setName("Player 2");
        player.setPlatformID(1234567);
        player.setShown(true);
        when(playerDAO.get(2)).thenReturn(player);

        player = new PlayerDTO();
        player.setId(3L);
        player.setName("Player 3");
        player.setPlatformID(12345678);
        player.setShown(true);
        when(playerDAO.get(3)).thenReturn(player);

        player = new PlayerDTO();
        player.setId(4L);
        player.setName("Player 4");
        player.setPlatformID(123456789);
        player.setShown(true);
        when(playerDAO.get(4)).thenReturn(player);

        jdbcMatchDAO = new JDBCMatchDAO(jdbcConnectionManager, playerDAO, folderDAO);

        //folderDAO = new UserFolderDAO("testParserDir", "testFileDir", "testHeatmapDir");
        teamDAO = new JDBCTeamDAO(jdbcConnectionManager, playerDAO, jdbcMatchDAO);
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
    public void readTeamsTest() throws TeamPersistenceException {
        List<TeamDTO> teams = teamDAO.readTeams();
        Assert.assertThat(teams.size(), is(2));
        TeamDTO team = teams.get(0);
        Assert.assertThat(team.getId(), is(1L));
        Assert.assertThat(team.getName(), is("Team 1"));
        Assert.assertThat(team.getTeamSize(), is(2));
    }

    @Test
    public void createTeamTest() throws TeamPersistenceException {
        TeamDTO team = new TeamDTO();
        team.setId(3L);
        team.setName("Team 3");
        team.setTeamSize(3);
        List<PlayerDTO> players = new ArrayList<>();
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        players.add(player);
        player = new PlayerDTO();
        player.setId(2L);
        players.add(player);
        player = new PlayerDTO();
        player.setId(3L);
        players.add(player);
        team.setPlayers(players);

        teamDAO.createTeam(team);

        List<TeamDTO> teams = teamDAO.readTeams();
        Assert.assertThat(teams.size(), is(3));
    }

    @Test
    public void deleteTeamTest() throws TeamPersistenceException {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setId(1L);
        List<PlayerDTO> players = new ArrayList<>();
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        players.add(player);
        player = new PlayerDTO();
        player.setId(2L);
        players.add(player);
        teamDTO.setPlayers(players);

        teamDAO.deleteTeam(teamDTO);

        Assert.assertThat(teamDAO.readTeams().size(), is(1));
    }

    @Test
    public void readPlayerTeamsTest() throws TeamPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);


        Assert.assertThat(teamDAO.readPlayerTeams(player).size(), is(1));

        player = new PlayerDTO();
        player.setId(5L);

        Assert.assertThat(teamDAO.readPlayerTeams(player).size(), is(0));
    }
}
