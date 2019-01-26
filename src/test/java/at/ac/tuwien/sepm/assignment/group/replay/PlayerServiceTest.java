package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerValidationException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimplePlayerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

public class PlayerServiceTest {

    private PlayerService playerService;
    private PlayerDAO playerDAO;
    private MatchDAO matchDAO;

    @Before
    public void setUp() {
        playerDAO = mock(PlayerDAO.class);
        matchDAO = mock(MatchDAO.class);
        playerService = new SimplePlayerService(playerDAO, matchDAO);
    }

    @Test
    public void createWithValidParameterShouldCallDAOMethod() throws PlayerServiceException, PlayerValidationException, PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setShown(true);
        player.setName("Test");
        player.setPlatformID(11111L);

        playerService.createPlayer(player);
        verify(playerDAO).createPlayer(player);
    }

    @Test(expected = PlayerValidationException.class)
    public void createWithNegativePlatformIDShouldThrowException() throws PlayerServiceException, PlayerValidationException {
        PlayerDTO player = new PlayerDTO();
        player.setName("Player 1");
        player.setPlatformID(-1234);

        playerService.createPlayer(player);
    }

    /**
     * This test tries to save a player with an empty name. A PlayerValidationException should be thrown
     **/
    @Test(expected = PlayerValidationException.class)
    public void createWithEmptyNameShouldThrowException() throws PlayerServiceException, PlayerValidationException {
        PlayerDTO player = new PlayerDTO();
        player.setName("");
        player.setPlatformID(123456789);

        playerService.createPlayer(player);
    }

    @Test(expected = PlayerServiceException.class)
    public void createWithPersistenceException() throws PlayerServiceException, PlayerValidationException, PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(20L);
        player.setPlatformID(1222);
        player.setName("Test");

        when(playerDAO.createPlayer(any(PlayerDTO.class))).thenThrow(PlayerPersistenceException.class);
        playerService.createPlayer(player);
    }

    @Test(expected = PlayerValidationException.class)
    public void deleteWithEmptyListShouldThrowException() throws PlayerServiceException, PlayerValidationException {
        List<PlayerDTO> players = new ArrayList<>();
        playerService.deletePlayers(players);
    }

    @Test(expected = PlayerValidationException.class)
    public void deleteWithNullShouldThrowException() throws PlayerServiceException, PlayerValidationException {
        playerService.deletePlayers(null);
    }

    @Test
    public void deleteWithValidParameterShouldCallDAOMethod() throws PlayerServiceException, PlayerValidationException, PlayerPersistenceException {
        PlayerDTO player1 = new PlayerDTO();
        player1.setId(1L);
        PlayerDTO player2 = new PlayerDTO();
        player2.setId(2L);
        List<PlayerDTO> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        playerService.deletePlayers(players);
        verify(playerDAO, times(2)).deletePlayer(any(PlayerDTO.class));
    }

    @Test(expected = PlayerServiceException.class)
    public void deleteWithPersistenceException() throws PlayerPersistenceException, PlayerServiceException, PlayerValidationException {
        doThrow(PlayerPersistenceException.class).when(playerDAO).deletePlayer(any(PlayerDTO.class));

        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        List<PlayerDTO> players = new ArrayList<>();
        players.add(player);
        playerService.deletePlayers(players);
    }

    @Test
    public void getPlayersShouldCallDAOMethod() throws PlayerServiceException, PlayerPersistenceException {
        playerService.getPlayers();
        verify(playerDAO).readPlayers();
    }

    @Test(expected = PlayerServiceException.class)
    public void getPlayersWithPersistenceException() throws PlayerPersistenceException, PlayerServiceException {
        when(playerDAO.readPlayers()).thenThrow(PlayerPersistenceException.class);
        playerService.getPlayers();
    }

    @Test
    public void showPlayerShouldCallDAOMethod() throws PlayerServiceException, PlayerPersistenceException {
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        playerService.showPlayer(player);
        verify(playerDAO).showPlayer(player);
    }
    @Test(expected = PlayerServiceException.class)
    public void showPlayerWithPersistenceException() throws PlayerPersistenceException, PlayerServiceException {
        doThrow(PlayerPersistenceException.class).when(playerDAO).showPlayer(any(PlayerDTO.class));
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        playerService.showPlayer(player);
    }

    /**
     * This test checks if the wins and losses are calculated correct
     *
     */
    @Test
    public void getAvgStatsServiceTest() throws Exception {

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

        AvgStatsDTO result = playerService.getAvgStats(playerDTO, matchType);
        Assert.assertThat(result.getWins(), is(0));
        Assert.assertThat(result.getLosses(), is(1));
    }

    @Test(expected = PlayerServiceException.class)
    public void getAvgStatsWithPersistenceException() throws PlayerPersistenceException, PlayerServiceException {
        when(playerDAO.getAvgStats(any(PlayerDTO.class), any(MatchType.class))).thenThrow(PlayerPersistenceException.class);
        playerService.getAvgStats(new PlayerDTO(), MatchType.RANKED2V2);
    }
}
