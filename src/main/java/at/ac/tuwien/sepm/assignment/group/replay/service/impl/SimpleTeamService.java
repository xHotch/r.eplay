package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Markus Kogelbauer
 */
@Component
public class SimpleTeamService implements TeamService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TeamDAO teamDAO;

    public SimpleTeamService(TeamDAO teamDAO) {
        this.teamDAO = teamDAO;
    }

    @Override
    public void createTeam(TeamDTO teamDTO) throws TeamValidationException, TeamServiceException {
        LOG.trace("Called - createTeam");
        teamDTOValidator(teamDTO);
        try {
            teamDAO.createTeam(teamDTO);
        } catch (TeamPersistenceException e) {
            String msg = "Failed to create team";
            throw new TeamServiceException(msg, e);
        }
    }

    @Override
    public List<TeamDTO> readTeams() throws TeamServiceException {
        try {
            return teamDAO.readTeams();
        } catch (TeamPersistenceException e) {
            throw new TeamServiceException("failed to read teams", e);
        }
    }

    @Override
    public TeamCompareDTO readTeamMatches(TeamDTO teamDTO1, TeamDTO teamDTO2) throws TeamServiceException {
        try {
            TeamCompareDTO teamCompareDTO = new TeamCompareDTO();
            teamCompareDTO.setMatchDTOList(teamDAO.readTeamMatches(teamDTO1, teamDTO2));
            teamCompareDTO.setMatchStatsDTOList(teamDAO.readTeamStats(teamDTO1, teamDTO2));
            //Map<MatchId,Map<TeamSide,TeamId>>
            Map<Integer, Map<TeamSide, Long>> teamSideToTeamId = new HashMap<>();
            teamCompareDTO.getMatchDTOList().forEach(match -> teamSideToTeamId.put(match.getId(), mapTeamSideToTeamID(match, teamDTO1, teamDTO2)));

            //Map teamSide from matchStats to the right TeamDTO
            teamCompareDTO.getMatchStatsDTOList().forEach((matchID, mapSideToID) -> mapSideToID.forEach(matchStatsDTO -> matchStatsDTO.setTeamId(teamSideToTeamId.get(matchID).get(matchStatsDTO.getTeam()))));

            return teamCompareDTO;
        } catch (TeamPersistenceException e) {
            throw new TeamServiceException("failed to read team stats", e);
        }
    }

    @Override
    public PlayerTeamsDTO readPlayerTeams(PlayerDTO playerDTO) throws TeamServiceException {
        PlayerTeamsDTO playerTeamsDTO = new PlayerTeamsDTO();
        playerTeamsDTO.setPlayerDTO(playerDTO);
        try {
            playerTeamsDTO.setTeams(teamDAO.readPlayerTeams(playerDTO));
        } catch (TeamPersistenceException e) {
            throw new TeamServiceException("failed to read teams", e);
        }
        return playerTeamsDTO;
    }

    /**
     * Maps both teamSide from the matchDTO to a TeamID
     * @param matchDTO the match
     * @param team1DTO the first team
     * @param team2DTO the second team
     * @return A Map withe teamSide as key and the teamId as value
     */
    private Map<TeamSide, Long> mapTeamSideToTeamID(MatchDTO matchDTO, TeamDTO team1DTO, TeamDTO team2DTO) {
        Map<TeamSide, Long> teamSideToTeamId = new EnumMap<>(TeamSide.class);
        List<MatchPlayerDTO> matchPlayerDTOList = matchDTO.getPlayerData().stream().filter(p -> p.getTeam() == TeamSide.RED).collect(Collectors.toList());
        if (matchPlayerDTOList.stream().map(MatchPlayerDTO::getPlayerDTO).anyMatch(p -> team1DTO.getPlayers().contains(p))) {
            teamSideToTeamId.put(TeamSide.RED, team1DTO.getId());
            teamSideToTeamId.put(TeamSide.BLUE, team2DTO.getId());
        } else {
            teamSideToTeamId.put(TeamSide.RED, team2DTO.getId());
            teamSideToTeamId.put(TeamSide.BLUE, team1DTO.getId());
        }
        return teamSideToTeamId;
    }

    @Override
    public void deleteTeam(TeamDTO teamDTO) throws TeamValidationException, TeamServiceException {
        LOG.trace("Called - deleteTeam");
        teamDTOValidator(teamDTO);
        try {
            teamDAO.deleteTeam(teamDTO);
        } catch (TeamPersistenceException e) {
            String msg = "Failed to delete team";
            throw new TeamServiceException(msg, e);
        }
    }

    private void teamDTOValidator(TeamDTO teamDTO) throws TeamValidationException {
        LOG.trace("Called - teamDTOValidator");
        String errMsg = "";
        if (teamDTO.getName() == null || teamDTO.getName().equals("")) {
            errMsg += "No name\n";
        }
        if (teamDTO.getTeamSize() <= 0) {
            errMsg += "TeamSize is smaller or equal to zero\n";
        }
        if (teamDTO.getTeamSize() > 3) {
            errMsg += "TeamSize is bigger as 3\n";
        }
        if (teamDTO.getPlayers().size() != teamDTO.getTeamSize()) {
            errMsg += "Amount of selected players must fit team size\n";
        }
        if (!errMsg.equals("")) {
            throw new TeamValidationException(errMsg);
        }
    }
}
