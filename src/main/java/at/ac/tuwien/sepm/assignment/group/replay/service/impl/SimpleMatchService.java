package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Daniel Klampfl
 */
@Component
public class SimpleMatchService implements MatchService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MatchDAO matchDAO;
    private FolderDAO folderDAO;


    public SimpleMatchService(MatchDAO matchDAO, FolderDAO folderDAO) {
        this.matchDAO = matchDAO;
        this.folderDAO = folderDAO;

    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws MatchValidationException, MatchServiceException, ReplayAlreadyExistsException {
        LOG.trace("Called - createMatch");
        matchDTOValidator(matchDTO);
        try {
            matchDAO.createMatch(matchDTO);
        } catch (MatchPersistenceException e) {
            String msg = "Failed to create match";
            throw new MatchServiceException(msg,e);
        } catch (MatchAlreadyExistsException e) {
            throw new ReplayAlreadyExistsException("Replay already exists",e);
        }
    }

    @Override
    public List<MatchDTO> getMatches() throws MatchServiceException{
        LOG.trace("Called - getMatches");
        try {
            return matchDAO.readMatches();
        } catch (MatchPersistenceException e){
            String message = "Could not get matches from DAO";
            throw new MatchServiceException(message,e);
        }
    }

    @Override
    public void deleteMatch(MatchDTO matchDTO) throws MatchServiceException {
        LOG.trace("called - deleteMatch");
        try {
            matchDAO.deleteMatch(matchDTO);
        } catch (MatchPersistenceException e) {
            throw new MatchServiceException("could not delete match",e);
        }
    }

    @Override
    public void deleteFile(File file) throws FileServiceException {
        LOG.trace("Called - deleteFile");
        if(file == null) return;
        try {
            folderDAO.deleteFile(file);
        } catch (FilePersistenceException e){
            throw new FileServiceException("Could not delete File", e);
        }
    }

    @Override
    public List<MatchDTO> searchMatches(String name, LocalDateTime begin, LocalDateTime end, int teamSize) throws MatchServiceException, FilterValidationException {
        LOG.trace("Called - searchMatches");
        searchParamValidator(begin, end, teamSize);
        try {
            return matchDAO.searchMatches(name, begin, end, teamSize);
        } catch (MatchPersistenceException e){
            String message = "Could not get matches from DAO";
            throw new MatchServiceException(message,e);
        }
    }

    @Override
    public MatchStatsDTO calcTeamStats(MatchDTO matchDTO, TeamSide teamSide) {
        MatchStatsDTO matchStatsDTO = new MatchStatsDTO();
        matchStatsDTO.setTeam(teamSide);
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
            if (matchPlayerDTO.getTeam() == teamSide) {
                matchStatsDTO.setAssists(matchStatsDTO.getAssists() + matchPlayerDTO.getAssists());
                matchStatsDTO.setAverageSpeed(matchStatsDTO.getAverageSpeed() + matchPlayerDTO.getAverageSpeed());
                matchStatsDTO.setGoals(matchStatsDTO.getGoals() + matchPlayerDTO.getGoals());
                matchStatsDTO.setSaves(matchStatsDTO.getSaves() + matchPlayerDTO.getSaves());
                matchStatsDTO.setScore(matchStatsDTO.getScore() + matchPlayerDTO.getScore());
                matchStatsDTO.setShots(matchStatsDTO.getShots() + matchPlayerDTO.getShots());
                matchStatsDTO.setBoostPadAmount(matchStatsDTO.getBoostPadAmount() + matchPlayerDTO.getBoostPadAmount());
                matchStatsDTO.setBoostPerMinute(matchStatsDTO.getBoostPerMinute() + matchPlayerDTO.getBoostPerMinute());
            }
        }
        matchStatsDTO.setAverageSpeed(matchStatsDTO.getAverageSpeed() / (matchDTO.getPlayerData().size() / 2.0));
        matchStatsDTO.setBoostPerMinute(matchStatsDTO.getBoostPerMinute() / (matchDTO.getPlayerData().size() / 2.0));
        return matchStatsDTO;
    }

    @Override
    public void getHeatmaps(MatchDTO matchDTO) throws FileServiceException {
        LOG.trace("Called - getHeatmaps");
        try {
            folderDAO.getHeatmaps(matchDTO);
        } catch (FilePersistenceException e){
            throw new FileServiceException("Could read heatmaps", e);
        }
    }


    private void matchDTOValidator(MatchDTO matchDTO) throws MatchValidationException {
        LOG.trace("Called - matchDTOValidator");
        String errMsg = "";

        if (matchDTO.getDateTime() == null) errMsg += "Kein Matchdatum\n";
        if(matchDTO.getMatchTime() < 0) errMsg += "Matchdauer kann nicht negativ sein\n";
        if (matchDTO.getPlayerData() == null || matchDTO.getPlayerData().isEmpty()) errMsg += "Keine Spieler im Match gefunden\n";
        else {
            if (matchDTO.getPlayerData().size() != matchDTO.getTeamSize() * 2) errMsg += "Teamgröße stimmt nicht mit Spielerliste überein\n";
            int countTeamBlue = 0;
            int countTeamRed = 0;
            for (MatchPlayerDTO player : matchDTO.getPlayerData()) {
                errMsg += matchPlayerDTOValidator(player);
                if (player.getTeam() == TeamSide.RED) {
                    countTeamRed++;
                }
                if (player.getTeam() == TeamSide.BLUE) {
                    countTeamBlue++;
                }
            }
            if (matchDTO.getTeamSize() != countTeamBlue || matchDTO.getTeamSize() != countTeamRed) errMsg += "Ungerade Teamgröße\n";
        }
        if (!errMsg.equals("")) {
            throw new MatchValidationException(errMsg);
        }
    }

    private String matchPlayerDTOValidator(MatchPlayerDTO matchPlayerDTO) {
        LOG.trace("Called - matchPlayerDTOValidator");
        String errMsg = "";
        if (matchPlayerDTO.getName() == null || matchPlayerDTO.getName().equals("")) errMsg += "Kein Name\n";
        if (matchPlayerDTO.getGoals() < 0) errMsg += "Tore negativ\n";
        if (matchPlayerDTO.getShots() < 0) errMsg += "Schüsse negativ\n";
        if (matchPlayerDTO.getAssists() < 0) errMsg += "Vorlagen negativ\n";
        if (matchPlayerDTO.getSaves() < 0) errMsg += "Paraden negativ\n";
        if (matchPlayerDTO.getScore() < 0) errMsg += "Punkte negativ\n";
        return errMsg;
    }

    private void searchParamValidator(LocalDateTime begin, LocalDateTime end, int teamSize) throws FilterValidationException {
        LOG.trace("Called - searchParamValidator");
        String errMsg = "";
        if ((begin == null && end != null) || (end == null && begin != null)) {
            errMsg += "Beginn und Ende müssen entweder beide oder keine gesetzt sein\n";
        }
        if (begin != null && end != null && begin.isAfter(end)) {
            errMsg += "Beginn muss vor Ende sein\n";
        }
        if (teamSize < 0 || teamSize > 3) {
            errMsg += "Teamgröße muss zwischen 0 und 3 sein\n";
        }
        if (!errMsg.equals("")) {
            throw new FilterValidationException(errMsg);
        }
    }
}
