package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleTeamService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
