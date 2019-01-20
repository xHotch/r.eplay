package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamCompareDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

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
            teamCompareDTO.setMatchDTOList(teamDAO.readTeamMatches(teamDTO1,teamDTO2));
            teamCompareDTO.setMatchStatsDTOList(teamDAO.readTeamStats(teamDTO1,teamDTO2));
            Map<Integer, List<MatchStatsDTO>> matchIDToMatchStats = teamCompareDTO.getMatchStatsDTOList();
            //TODO tie matchstats to team
            return teamCompareDTO;
        } catch (TeamPersistenceException e) {
            throw new TeamServiceException("failed to read team stats",e);
        }
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
