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
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
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
import java.util.Map;

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

        folderDAO = new UserFolderDAO("testParserDir", "testFileDir", "testHeatmapDir");

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

    @Test(expected = TeamPersistenceException.class)
    public void readTeamsWithoutConnectionShouldThrowException() throws TeamPersistenceException {
        jdbcConnectionManager.closeConnection();
        teamDAO.readTeams();
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

    @Test(expected = TeamPersistenceException.class)
    public void createTeamWithoutConnectionShouldThrowException() throws TeamPersistenceException {
        jdbcConnectionManager.closeConnection();
        teamDAO.createTeam(new TeamDTO());
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

    @Test(expected = TeamPersistenceException.class)
    public void deleteTeamWithoutConnectionShouldThrowException() throws TeamPersistenceException {
        jdbcConnectionManager.closeConnection();
        teamDAO.deleteTeam(new TeamDTO());
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

    @Test(expected = TeamPersistenceException.class)
    public void readPlayerTeamsWithoutConnectionShouldThrowException() throws TeamPersistenceException {
        jdbcConnectionManager.closeConnection();
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        teamDAO.readPlayerTeams(player);
    }
    
    @Test
    public void readTeamMatchesTest() throws TeamPersistenceException {
        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setId(1L);
        
        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2L);
        
        List<MatchDTO> matchDTOS = teamDAO.readTeamMatches(teamDTO1,teamDTO2);

        Assert.assertThat(matchDTOS.size(),is(2));
    }

    @Test(expected = TeamPersistenceException.class)
    public void readTeamMatchesWithoutConnectionShouldThrowException() throws TeamPersistenceException {
        jdbcConnectionManager.closeConnection();
        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setId(1L);

        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2L);
        teamDAO.readTeamMatches(teamDTO1,teamDTO2);
    }


    @Test
    public void readTeamStatsTest() throws TeamPersistenceException {
        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setId(1L);

        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2L);

        Map<Integer, List<MatchStatsDTO>> listMap = teamDAO.readTeamStats(teamDTO1,teamDTO2);

        Assert.assertThat(listMap.size(),is(2));

        List<MatchStatsDTO> matchStatsDTOS = listMap.get(1);
        Assert.assertThat(matchStatsDTOS.size(), is(2));

        MatchStatsDTO statsBlueTeam;
        MatchStatsDTO statsRedTeam;
        if (matchStatsDTOS.get(0).getTeam() == TeamSide.BLUE) {
            statsBlueTeam = matchStatsDTOS.get(0);
            statsRedTeam = matchStatsDTOS.get(1);
        } else {
            statsBlueTeam = matchStatsDTOS.get(1);
            statsRedTeam = matchStatsDTOS.get(0);
        }
        Assert.assertThat(statsBlueTeam.getGoals(),is(2));
        Assert.assertThat(statsBlueTeam.getAssists(),is(1));
        Assert.assertThat(statsBlueTeam.getSaves(),is(0));
        Assert.assertThat(statsBlueTeam.getShots(),is(4));
        Assert.assertThat(statsBlueTeam.getScore(),is(300));
        Assert.assertThat(statsBlueTeam.getAverageSpeed(),is(1325.0));
        Assert.assertThat(statsBlueTeam.getBoostPadAmount(),is(105));
        Assert.assertThat(statsBlueTeam.getBoostPerMinute(),is(270.0));

        Assert.assertThat(statsRedTeam.getGoals(),is(1));
        Assert.assertThat(statsRedTeam.getAssists(),is(1));
        Assert.assertThat(statsRedTeam.getSaves(),is(1));
        Assert.assertThat(statsRedTeam.getShots(),is(3));
        Assert.assertThat(statsRedTeam.getScore(),is(220));
        Assert.assertThat(statsRedTeam.getAverageSpeed(),is(1345.0));
        Assert.assertThat(statsRedTeam.getBoostPadAmount(),is(125));
        Assert.assertThat(statsRedTeam.getBoostPerMinute(),is(305.0));


        matchStatsDTOS = listMap.get(2);
        Assert.assertThat(matchStatsDTOS.size(), is(2));
        if (matchStatsDTOS.get(0).getTeam() == TeamSide.BLUE) {
            statsBlueTeam = matchStatsDTOS.get(0);
            statsRedTeam = matchStatsDTOS.get(1);
        } else {
            statsBlueTeam = matchStatsDTOS.get(1);
            statsRedTeam = matchStatsDTOS.get(0);
        }
        Assert.assertThat(statsBlueTeam.getGoals(),is(4));
        Assert.assertThat(statsBlueTeam.getAssists(),is(2));
        Assert.assertThat(statsBlueTeam.getSaves(),is(0));
        Assert.assertThat(statsBlueTeam.getShots(),is(7));
        Assert.assertThat(statsBlueTeam.getScore(),is(700));
        Assert.assertThat(statsBlueTeam.getAverageSpeed(),is(1325.0));
        Assert.assertThat(statsBlueTeam.getBoostPadAmount(),is(110));
        Assert.assertThat(statsBlueTeam.getBoostPerMinute(),is(265.0));

        Assert.assertThat(statsRedTeam.getGoals(),is(1));
        Assert.assertThat(statsRedTeam.getAssists(),is(1));
        Assert.assertThat(statsRedTeam.getSaves(),is(1));
        Assert.assertThat(statsRedTeam.getShots(),is(3));
        Assert.assertThat(statsRedTeam.getScore(),is(300));
        Assert.assertThat(statsRedTeam.getAverageSpeed(),is(1345.0));
        Assert.assertThat(statsRedTeam.getBoostPadAmount(),is(150));
        Assert.assertThat(statsRedTeam.getBoostPerMinute(),is(285.0));
    }

    @Test(expected = TeamPersistenceException.class)
    public void readTeamStatsWithoutConnectionShouldThrowException() throws TeamPersistenceException {
        jdbcConnectionManager.closeConnection();
        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setId(1L);

        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2L);
        teamDAO.readTeamStats(teamDTO1,teamDTO2);
    }
}
