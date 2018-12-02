package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@Component
public class JDBCMatchDAO implements MatchDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INSERT_MATCH = "INSERT INTO match_ SET dateTime = ?, teamSize = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET  playerid = ?, matchid = ?, name = ?, team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?";

    private static final String READ_ALL_MATCHES = "SELECT * FROM match_";
    private static final String READ_PLAYERS_FROM_MATCHES = "SELECT * FROM matchPlayer WHERE matchid = ?";

    private final Connection connection;

    public JDBCMatchDAO(JDBCConnectionManager jdbcConnectionManager) {
       this.connection = jdbcConnectionManager.getConnection();
    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws MatchPersistenceException {
        LOG.trace("Called - createMatch");
        int matchID;
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MATCH, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(matchDTO.getDateTime()));
            ps.setInt(2, matchDTO.getTeamSize());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                matchID = rs.getInt("id");
            } catch (SQLException e) {
                String msg = "Could not read resultSet of match";
                LOG.error(msg, e);
                throw new MatchPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not create match";
            LOG.error(msg, e);
            throw new MatchPersistenceException(msg, e);
        }
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
            matchPlayerDTO.setMatchId(matchID);
            createMatchPlayer(matchPlayerDTO);
        }
    }

    private void createMatchPlayer(MatchPlayerDTO matchPlayerDTO) throws MatchPersistenceException {
        LOG.trace("Called - createMatchPlayer");
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MATCH_PLAYER)) {

            ps.setInt(1,matchPlayerDTO.getPlayerId());
            ps.setInt(2,matchPlayerDTO.getMatchId());
            ps.setString(3, matchPlayerDTO.getName());
            ps.setInt(4, matchPlayerDTO.getTeam());
            ps.setInt(5, matchPlayerDTO.getScore());
            ps.setInt(6, matchPlayerDTO.getGoals());
            ps.setInt(7, matchPlayerDTO.getAssists());
            ps.setInt(8, matchPlayerDTO.getSaves());
            ps.setInt(9, matchPlayerDTO.getShots());

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Could not create matchPlayer";
            LOG.error(msg, e);
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

                    // retrieve the players from the match
                    List<MatchPlayerDTO> matchPlayers = readMatchPlayers(match.getId());
                    match.setPlayerData(matchPlayers);

                    result.add(match);
                    LOG.debug("Added match to the result list!");
                }
            } catch (SQLException e) {
                String msg = "Could not read resultSet of match";
                LOG.error(msg, e);
                throw new MatchPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not read match";
            LOG.error(msg, e);
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }

    private List<MatchPlayerDTO> readMatchPlayers(int matchId) throws MatchPersistenceException {
        LOG.trace("Called - readMatchPlayers");
        List<MatchPlayerDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_PLAYERS_FROM_MATCHES, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,matchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatchPlayerDTO matchPlayer = new MatchPlayerDTO();
                    matchPlayer.setMatchId(rs.getInt("matchId"));
                    matchPlayer.setPlayerId(rs.getInt("playerId"));
                    matchPlayer.setName(rs.getString("name"));
                    matchPlayer.setTeam(rs.getInt("team"));
                    matchPlayer.setScore(rs.getInt("score"));
                    matchPlayer.setGoals(rs.getInt("goals"));
                    matchPlayer.setAssists(rs.getInt("assists"));
                    matchPlayer.setSaves(rs.getInt("saves"));
                    matchPlayer.setShots(rs.getInt("shots"));

                    result.add(matchPlayer);
                }
            } catch (SQLException e) {
                String msg = "Could not read resultSet of match players";
                LOG.error(msg, e);
                throw new MatchPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not read match players";
            LOG.error(msg, e);
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }
}
