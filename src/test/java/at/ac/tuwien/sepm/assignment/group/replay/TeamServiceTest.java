package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleTeamService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TeamServiceTest {

    private TeamService teamService;
    private TeamDAO teamDAO;

    @Before
    public void setUp() {
        teamDAO = mock(TeamDAO.class);
        teamService = new SimpleTeamService(teamDAO);
    }

    @Test
    public void createTeamWithValidParameterShouldCallDAOMethod() throws TeamServiceException, TeamValidationException, TeamPersistenceException {
        TeamDTO team = getValidTeam();
        teamService.createTeam(team);
        verify(teamDAO).createTeam(team);
    }

    @Test(expected = TeamServiceException.class)
    public void createTeamWithPersistenceException() throws TeamPersistenceException, TeamServiceException, TeamValidationException {
        doThrow(TeamPersistenceException.class).when(teamDAO).createTeam((any(TeamDTO.class)));
        teamService.createTeam(getValidTeam());
    }

    @Test(expected = TeamValidationException.class)
    public void createTeamWithoutNameShouldThrowException() throws TeamServiceException, TeamValidationException {
        TeamDTO team = getValidTeam();
        team.setName(null);
        teamService.createTeam(team);
    }

    @Test(expected = TeamValidationException.class)
    public void createTeamWith4PlayerShouldThrowException() throws TeamServiceException, TeamValidationException {
        TeamDTO team = getValidTeam();
        team.setTeamSize(4);
        teamService.createTeam(team);
    }

    @Test(expected = TeamValidationException.class)
    public void createTeamWithLessPlayerThanSizeShouldThrowException() throws TeamServiceException, TeamValidationException {
        TeamDTO team = getValidTeam();
        team.setTeamSize(2);
        teamService.createTeam(team);
    }

    @Test
    public void deleteTeamWithValidParameterShouldCallDAOMethod() throws TeamServiceException, TeamValidationException, TeamPersistenceException {
        TeamDTO team = getValidTeam();
        teamService.deleteTeam(team);
        verify(teamDAO).deleteTeam(team);
    }

    @Test(expected = TeamServiceException.class)
    public void deleteTeamWithPersistenceException() throws TeamPersistenceException, TeamServiceException, TeamValidationException {
        doThrow(TeamPersistenceException.class).when(teamDAO).deleteTeam((any(TeamDTO.class)));
        teamService.deleteTeam(getValidTeam());
    }

    @Test(expected = TeamValidationException.class)
    public void deleteTeamWithInvalidParameterShouldThrowException() throws TeamServiceException, TeamValidationException {
        TeamDTO team = getValidTeam();
        team.setPlayers(new ArrayList<>());
        teamService.deleteTeam(team);
    }

    @Test
    public void readTeamsShouldCallDAOMethod() throws TeamPersistenceException, TeamServiceException {
        teamService.readTeams();
        verify(teamDAO).readTeams();
    }

    @Test(expected = TeamServiceException.class)
    public void readTeamsWithPersistenceException() throws TeamServiceException, TeamPersistenceException {
        when(teamDAO.readTeams()).thenThrow(TeamPersistenceException.class);
        teamService.readTeams();
    }

    @Test
    public void readPlayerTeamsShouldCallDAOMethod() throws TeamPersistenceException, TeamServiceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        teamService.readPlayerTeams(player);
        verify(teamDAO).readPlayerTeams(player);
    }

    @Test(expected = TeamServiceException.class)
    public void readPlayerTeamsWithPersistenceException() throws TeamServiceException, TeamPersistenceException {
        when(teamDAO.readPlayerTeams(any(PlayerDTO.class))).thenThrow(TeamPersistenceException.class);
        teamService.readPlayerTeams(new PlayerDTO());
    }

    @Test
    public void readTeamMatchesShouldCallDAOMethod() throws TeamPersistenceException, TeamServiceException {
        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setId(1L);

        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2L);
        teamService.readTeamMatches(teamDTO1,teamDTO2);
        verify(teamDAO).readTeamMatches(teamDTO1,teamDTO2);
        verify(teamDAO).readTeamStats(teamDTO1,teamDTO2);
    }

    @Test
    public void readTeamMatchesTest() throws TeamPersistenceException, TeamServiceException {
        MatchDTO match = new MatchDTO();

        TeamDTO teamDTO1 = new TeamDTO();
        teamDTO1.setId(1L);

        List<MatchPlayerDTO> matchPlayerList = new ArrayList<>();
        List<PlayerDTO> players = new ArrayList<>();
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        MatchPlayerDTO matchPlayer = new MatchPlayerDTO();
        matchPlayer.setTeam(TeamSide.BLUE);
        matchPlayer.setPlayerDTO(player);
        matchPlayer.setMatchDTO(match);
        matchPlayerList.add(matchPlayer);
        players.add(player);
        player = new PlayerDTO();
        player.setId(2L);
        matchPlayer = new MatchPlayerDTO();
        matchPlayer.setTeam(TeamSide.BLUE);
        matchPlayer.setPlayerDTO(player);
        matchPlayer.setMatchDTO(match);
        matchPlayerList.add(matchPlayer);
        players.add(player);
        teamDTO1.setPlayers(players);

        players = new ArrayList<>();
        TeamDTO teamDTO2 = new TeamDTO();
        teamDTO2.setId(2L);
        player = new PlayerDTO();
        player.setId(3L);
        matchPlayer = new MatchPlayerDTO();
        matchPlayer.setTeam(TeamSide.RED);
        matchPlayer.setPlayerDTO(player);
        matchPlayer.setMatchDTO(match);
        matchPlayerList.add(matchPlayer);
        players.add(player);
        player = new PlayerDTO();
        player.setId(4L);
        matchPlayer = new MatchPlayerDTO();
        matchPlayer.setTeam(TeamSide.RED);
        matchPlayer.setPlayerDTO(player);
        matchPlayer.setMatchDTO(match);
        matchPlayerList.add(matchPlayer);
        players.add(player);
        teamDTO2.setPlayers(players);

        match.setId(1);
        match.setPlayerData(matchPlayerList);

        List<MatchDTO> matchList = new LinkedList<>();
        matchList.add(match);

        Map<Integer, List<MatchStatsDTO>> mapStatList = new HashMap<>();

        List<MatchStatsDTO> matchStatsDTOList = new ArrayList<>();
        MatchStatsDTO stats = new MatchStatsDTO();
        stats.setTeam(TeamSide.BLUE);
        stats.setMatchId(1);
        matchStatsDTOList.add(stats);

        stats = new MatchStatsDTO();
        stats.setTeam(TeamSide.RED);
        stats.setMatchId(1);
        matchStatsDTOList.add(stats);

        mapStatList.put(1,matchStatsDTOList);
        when(teamDAO.readTeamMatches(teamDTO1,teamDTO2)).thenReturn(matchList);
        when(teamDAO.readTeamStats(teamDTO1,teamDTO2)).thenReturn(mapStatList);

        TeamCompareDTO teamCompareDTO = teamService.readTeamMatches(teamDTO1,teamDTO2);

        Assert.assertThat(teamCompareDTO.getMatchStatsDTOList().size(), is(1));
        Assert.assertThat(teamCompareDTO.getMatchStatsDTOList().get(1).size(),is(2));
        MatchStatsDTO statsBlueTeam;
        MatchStatsDTO statsRedTeam;
        if (teamCompareDTO.getMatchStatsDTOList().get(1).get(0).getTeam() == TeamSide.BLUE) {
            statsBlueTeam = teamCompareDTO.getMatchStatsDTOList().get(1).get(0);
            statsRedTeam = teamCompareDTO.getMatchStatsDTOList().get(1).get(1);
        } else {
            statsBlueTeam = teamCompareDTO.getMatchStatsDTOList().get(1).get(1);
            statsRedTeam = teamCompareDTO.getMatchStatsDTOList().get(1).get(0);
        }
        Assert.assertThat(statsBlueTeam.getTeamId(),is(1L));
        Assert.assertThat(statsRedTeam.getTeamId(),is(2L));
    }

    @Test(expected = TeamServiceException.class)
    public void readTeamMatchesWithPersistenceException() throws TeamServiceException, TeamPersistenceException {
        when(teamDAO.readTeamMatches(any(TeamDTO.class),any(TeamDTO.class))).thenThrow(TeamPersistenceException.class);
        teamService.readTeamMatches(new TeamDTO(),new TeamDTO());
    }




    private TeamDTO getValidTeam() {
        TeamDTO team = new TeamDTO();
        team.setName("Test");
        team.setTeamSize(1);
        List<PlayerDTO> players = new ArrayList<>();
        PlayerDTO player = new PlayerDTO();
        player.setId(1);
        players.add(player);
        team.setPlayers(players);
        return team;
    }

}
