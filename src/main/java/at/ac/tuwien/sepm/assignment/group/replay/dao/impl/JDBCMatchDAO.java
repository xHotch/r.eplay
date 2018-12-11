package at.ac.tuwien.sepm.assignment.group.replay.dao.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@Component
public class JDBCMatchDAO implements MatchDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INSERT_MATCH = "INSERT INTO match_ SET dateTime = ?, teamSize = ?, readId = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET  playerid = ?, matchid = ?, name = ?, team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?";

    private static final String READ_ALL_MATCHES = "SELECT * FROM match_";
    private static final String READ_PLAYERS_FROM_MATCHES = "SELECT * FROM matchPlayer WHERE matchid = ?";

    private static final String READ_MATCH_BY_READID = "Select id from match_ where readId = ?";

    private final Connection connection;

    private PlayerDAO playerDAO;

    public JDBCMatchDAO(JDBCConnectionManager jdbcConnectionManager, PlayerDAO playerDAO) throws SQLException {
        this.connection = jdbcConnectionManager.getConnection();
        this.playerDAO = playerDAO;
    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws MatchPersistenceException, MatchAlreadyExistsException {
        LOG.trace("Called - createMatch");
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MATCH, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(READ_MATCH_BY_READID)) {

            ps2.setString(1, matchDTO.getReadId());
            try (ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    throw new MatchAlreadyExistsException("Match already exists");
                }
            }

            ps.setTimestamp(1, Timestamp.valueOf(matchDTO.getDateTime()));
            ps.setInt(2, matchDTO.getTeamSize());
            ps.setString(3, matchDTO.getReadId());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                matchDTO.setId(rs.getInt("id"));
            }
        } catch (SQLException e) {
            String msg = "Could not create match";
            throw new MatchPersistenceException(msg, e);
        }
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {

            //todo vlt löschen
            matchPlayerDTO.setMatchDTO(matchDTO);
            createMatchPlayer(matchPlayerDTO);
        }
    }

    private void createMatchPlayer(MatchPlayerDTO matchPlayerDTO) throws MatchPersistenceException {
        LOG.trace("Called - createMatchPlayer");
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MATCH_PLAYER)) {

            ps.setLong(1,matchPlayerDTO.getPlayerId());
            ps.setInt(2,matchPlayerDTO.getMatchId());
            ps.setString(3, matchPlayerDTO.getName());
            ps.setInt(4, matchPlayerDTO.getTeam().getId());
            ps.setInt(5, matchPlayerDTO.getScore());
            ps.setInt(6, matchPlayerDTO.getGoals());
            ps.setInt(7, matchPlayerDTO.getAssists());
            ps.setInt(8, matchPlayerDTO.getSaves());
            ps.setInt(9, matchPlayerDTO.getShots());

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Could not create matchPlayer";
            throw new MatchPersistenceException(msg, e);
        }
    }

    @Override
    public List<MatchDTO> readMatches() throws MatchPersistenceException {
        LOG.trace("Called - readMatches");
        List<MatchDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_ALL_MATCHES, Statement.RETURN_GENERATED_KEYS)) {

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {

                    MatchDTO match = new MatchDTO();
                    match.setId(rs.getInt("id"));
                    match.setDateTime(rs.getTimestamp("dateTime").toLocalDateTime());
                    match.setTeamSize(rs.getInt("teamSize"));
                    match.setReadId(rs.getString("readId"));

                    // retrieve the players from the match
                    List<MatchPlayerDTO> matchPlayers = readMatchPlayers(match);
                    match.setPlayerData(matchPlayers);

                    result.add(match);
                    LOG.debug("Added match to the result list!");
                }
            }
        } catch (SQLException e) {
            String msg = "Could not read match";
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }

    private List<MatchPlayerDTO> readMatchPlayers(MatchDTO match) throws MatchPersistenceException {
        LOG.trace("Called - readMatchPlayers");
        List<MatchPlayerDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_PLAYERS_FROM_MATCHES, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,match.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchPlayerDTO matchPlayer = new MatchPlayerDTO();

                    matchPlayer.setMatchDTO(match);
                    int id = rs.getInt("playerId");
                    try {
                        matchPlayer.setPlayerDTO(playerDAO.get(id));
                    } catch (PlayerPersistenceException e){
                        String msg = "Could not read Player with id: " + id;
                        throw new MatchPersistenceException(msg, e);
                    }

                    matchPlayer.setTeam(TeamSide.getById(rs.getInt("team")).get());
                    matchPlayer.setScore(rs.getInt("score"));
                    matchPlayer.setGoals(rs.getInt("goals"));
                    matchPlayer.setAssists(rs.getInt("assists"));
                    matchPlayer.setSaves(rs.getInt("saves"));
                    matchPlayer.setShots(rs.getInt("shots"));

                    result.add(matchPlayer);
                }
            }
        } catch (SQLException e) {
            String msg = "Could not read match players";
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }




}
