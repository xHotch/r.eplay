package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

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
