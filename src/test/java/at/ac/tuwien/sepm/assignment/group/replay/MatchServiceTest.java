package at.ac.tuwien.sepm.assignment.group.replay;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.SimpleMatchService;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MatchServiceTest {

    private MatchService matchService;
    private MatchDAO matchDAO;
    private FolderDAO folderDAO;

    @Before
    public void setUp() {
        matchDAO = mock(MatchDAO.class);
        folderDAO = mock(FolderDAO.class);
        matchService = new SimpleMatchService(matchDAO, folderDAO);
    }

    @Test
    public void createMatchWithValidParameterShouldCallDAOMethod() throws MatchServiceException, MatchValidationException, ReplayAlreadyExistsException, MatchPersistenceException, MatchAlreadyExistsException {
        MatchDTO match = getValidMatch();
        matchService.createMatch(match);
        verify(matchDAO).createMatch(match);
    }

    @Test(expected = MatchValidationException.class)
    public void createMatchWithoutDateShouldThrowException() throws MatchServiceException, MatchValidationException, ReplayAlreadyExistsException {
        MatchDTO match = getValidMatch();
        match.setDateTime(null);
        matchService.createMatch(match);
    }

    @Test(expected = MatchValidationException.class)
    public void createMatchWithoutPlayerDataShouldThrowException() throws MatchServiceException, MatchValidationException, ReplayAlreadyExistsException {
        MatchDTO match = getValidMatch();
        match.setPlayerData(null);
        matchService.createMatch(match);
    }

    @Test(expected = MatchValidationException.class)
    public void createMatchWithNegativeMatchTimeShouldThrowException() throws MatchServiceException, MatchValidationException, ReplayAlreadyExistsException {
        MatchDTO match = getValidMatch();
        match.setMatchTime(-1);
        matchService.createMatch(match);
    }

    @Test(expected = MatchValidationException.class)
    public void createMatchWithWrongTeamSizeShouldThrowException() throws MatchServiceException, MatchValidationException, ReplayAlreadyExistsException {
        MatchDTO match = getValidMatch();
        match.setTeamSize(2);
        matchService.createMatch(match);
    }

    @Test(expected = MatchValidationException.class)
    public void createMatchWithUnevenTeamsShouldThrowException() throws MatchServiceException, MatchValidationException, ReplayAlreadyExistsException {
        MatchDTO match = getValidMatch();
        match.getPlayerData().get(0).setTeam(TeamSide.RED);
        match.getPlayerData().get(1).setTeam(TeamSide.RED);
        matchService.createMatch(match);
    }

    @Test(expected = MatchServiceException.class)
    public void createMatchWithPersistenceException() throws MatchPersistenceException, MatchAlreadyExistsException, MatchServiceException, MatchValidationException, ReplayAlreadyExistsException {
        doThrow(MatchPersistenceException.class).when(matchDAO).createMatch(any(MatchDTO.class));
        matchService.createMatch(getValidMatch());
    }

    @Test
    public void getMatchesShouldCallDAOMethod() throws MatchServiceException, MatchPersistenceException {
        matchService.getMatches();
        verify(matchDAO).readMatches();
    }

    @Test(expected = MatchServiceException.class)
    public void getMatchesWithPersistenceException() throws MatchServiceException, MatchPersistenceException {
        when(matchDAO.readMatches()).thenThrow(MatchPersistenceException.class);
        matchService.getMatches();
    }

    @Test
    public void deleteMatchShouldCallDAOMethod() throws MatchServiceException, MatchPersistenceException {
        MatchDTO match = getValidMatch();
        matchService.deleteMatch(match);
        verify(matchDAO).deleteMatch(match);
    }

    @Test(expected = MatchServiceException.class)
    public void deleteMatchWithPersistenceException() throws MatchPersistenceException, MatchServiceException {
        doThrow(MatchPersistenceException.class).when(matchDAO).deleteMatch(any(MatchDTO.class));
        matchService.deleteMatch(getValidMatch());
    }

    @Test
    public void deleteFileShouldCallDAOMethod() throws FileServiceException, FilePersistenceException {
        File file = new File("Test");
        matchService.deleteFile(file);
        verify(folderDAO).deleteFile(file);
    }

    @Test(expected = FileServiceException.class)
    public void deleteFileWithPersistenceException() throws FileServiceException, FilePersistenceException {
        doThrow(FilePersistenceException.class).when(folderDAO).deleteFile(any(File.class));
        matchService.deleteFile(new File("test"));
    }

    @Test(expected = FilterValidationException.class)
    public void validateSearchParamNegativeTeamSizeTest() throws FilterValidationException, MatchServiceException {
        matchService.searchMatches(null, null, null, -1);
    }

    @Test(expected = FilterValidationException.class)
    public void validateSearchParamBeginAfterEndTest() throws FilterValidationException, MatchServiceException {
        LocalDateTime dateTime = LocalDateTime.now();
        matchService.searchMatches(null, dateTime, dateTime.minusDays(1), 0);
    }

    @Test
    public void searchMatchesShouldCallDAOMethod() throws MatchServiceException, MatchPersistenceException, FilterValidationException {
        matchService.searchMatches("xyz", null, null, 0);
        verify(matchDAO).searchMatches("xyz", null, null, 0);
    }

    @Test(expected = MatchServiceException.class)
    public void searchMatchesWithPersistenceException() throws MatchServiceException, MatchPersistenceException, FilterValidationException {
        when(matchDAO.searchMatches(anyString(), any(), any(), anyInt())).thenThrow(MatchPersistenceException.class);
        matchService.searchMatches("xyz", null, null, 0);
    }

    @Test
    public void calcTeamStatsTest() {
        MatchDTO match = new MatchDTO();
        MatchPlayerDTO player1 = new MatchPlayerDTO();
        MatchPlayerDTO player2 = new MatchPlayerDTO();
        MatchPlayerDTO player3 = new MatchPlayerDTO();
        MatchPlayerDTO player4 = new MatchPlayerDTO();
        player1.setTeam(TeamSide.RED);
        player2.setTeam(TeamSide.RED);
        player3.setTeam(TeamSide.BLUE);
        player4.setTeam(TeamSide.BLUE);
        player1.setGoals(1);
        player1.setShots(2);
        player1.setScore(220);
        player1.setAssists(0);
        player1.setSaves(0);
        player1.setAverageSpeed(1300);

        player2.setGoals(0);
        player2.setShots(1);
        player2.setScore(120);
        player2.setAssists(1);
        player2.setSaves(1);
        player2.setAverageSpeed(1400);

        List<MatchPlayerDTO> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);
        players.add(player4);
        match.setPlayerData(players);

        MatchStatsDTO result = matchService.calcTeamStats(match, TeamSide.RED);

        assertThat(result.getAssists(), is(1));
        assertThat(result.getGoals(), is(1));
        assertThat(result.getShots(), is(3));
        assertThat(result.getScore(), is(340));
        assertThat(result.getSaves(), is(1));
        assertThat(result.getAverageSpeed(), is(1350.0));
    }

    @Test
    public void getHeatmapsShouldCallDAOMethod() throws FileServiceException, FilePersistenceException {
        MatchDTO match = getValidMatch();
        matchService.getHeatmaps(match);
        verify(folderDAO).getHeatmaps(match);
    }

    @Test(expected = FileServiceException.class)
    public void getHeatmapWithPersistenceException() throws FileServiceException, FilePersistenceException {
        doThrow(FilePersistenceException.class).when(folderDAO).getHeatmaps(any(MatchDTO.class));
        matchService.getHeatmaps(getValidMatch());
    }

    private MatchDTO getValidMatch() {
        // set up a match entity and define the object variables
        MatchDTO matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // set fileName
        matchDTO.setReplayFilename("TestFile");

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        MatchPlayerDTO playerRED = new MatchPlayerDTO();
        MatchPlayerDTO playerBLUE = new MatchPlayerDTO();

        // create 2 players
        PlayerDTO playerB = new PlayerDTO();
        PlayerDTO playerR = new PlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED,matchDTO,playerR,"Player red",   7, TeamSide.RED,3, 10, 2,3, 5);
        setPlayerVariables(playerBLUE, matchDTO,playerB,"Player blue", 8,TeamSide.BLUE, 15, 4,2, 3, 7);

        playerMatchList.add(playerRED);
        playerMatchList.add(playerBLUE);
        matchDTO.setPlayerData(playerMatchList);
        matchDTO.setBallHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));

        // set the remaining match variables
        matchDTO.setTeamSize(1);

        matchDTO.setReadId("Test");

        matchDTO.setMatchTime(400);

        return matchDTO;
    }

    private void setPlayerVariables(MatchPlayerDTO player, MatchDTO match, PlayerDTO playerDTO, String name, int id, TeamSide team, int score, int goals, int assists, int shots, int saves){
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
        player.setHeatmapImage(new BufferedImage(200,200,BufferedImage.TYPE_INT_RGB));

        //set boost pad list
        Map<Integer, List<BoostPadDTO>> boostPadMap = new HashMap<>();
        for(int i=0; i<=33; i++) {
            boostPadMap.putIfAbsent(i, new LinkedList<>());
            boostPadMap.get(i).add(new BoostPadDTO(0.0, 0.0, 0, false));
        }
        player.setBoostPadMap(boostPadMap);
    }
}
