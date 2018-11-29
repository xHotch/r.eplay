package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PersistenceException;
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
    private static final String INSERT_MATCH = "INSERT INTO match_ SET dateTime = ?, teamBlueGoals = ?, teamRedGoals = ?, teamSize = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET name = ?, team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?";
    private static final String INSERT_PLAYER_IN_MATCH = "INSERT INTO playerInMatch SET playerid = ?, matchid = ?";

    private static final String READ_ALL_MATCHES = "SELECT * FROM match_";
    private static final String READ_PLAYERS_FROM_MATCHES = "SELECT * FROM matchPlayer WHERE id = ?";
    private static final String READ_ALLPLAYERS_FROM_MATCH = "SELECT * FROM playerInMatch WHERE matchid = ?";

    private final JDBCConnectionManager jdbcConnectionManager;

    public JDBCMatchDAO(JDBCConnectionManager jdbcConnectionManager) {
        this.jdbcConnectionManager = jdbcConnectionManager;
    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws PersistenceException {
        LOG.trace("Called - createMatch");
        int matchID;
        try (Connection connection = jdbcConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_MATCH, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(matchDTO.getDateTime()));
            ps.setInt(2, matchDTO.getTeamBlueGoals());
            ps.setInt(3, matchDTO.getTeamRedGoals());
            ps.setInt(4, matchDTO.getTeamSize());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                matchID = rs.getInt("id");
            } catch (SQLException e) {
                String msg = "Could not read resultSet of match";
                LOG.error(msg, e);
                throw new PersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not create match";
            LOG.error(msg, e);
            throw new PersistenceException(msg, e);
        }
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
            int playerID;
            playerID = createMatchPlayer(matchPlayerDTO);
            linkPlayerMatch(playerID,matchID);
        }
    }

    private int createMatchPlayer(MatchPlayerDTO matchPlayerDTO) throws PersistenceException {
        LOG.trace("Called - createMatchPlayer");
        int matchPlayerID;
        try (Connection connection = jdbcConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_MATCH_PLAYER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, matchPlayerDTO.getName());
            ps.setInt(2, matchPlayerDTO.getTeam());
            ps.setInt(3, matchPlayerDTO.getScore());
            ps.setInt(4, matchPlayerDTO.getGoals());
            ps.setInt(5, matchPlayerDTO.getAssists());
            ps.setInt(6, matchPlayerDTO.getSaves());
            ps.setInt(7, matchPlayerDTO.getShots());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();

                matchPlayerID = rs.getInt("id");
            } catch (SQLException e) {
                String msg = "Could not read resultSet of matchPlayer";
                LOG.error(msg, e);
                throw new PersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not create matchPlayer";
            LOG.error(msg, e);
            throw new PersistenceException(msg, e);
        }
        return matchPlayerID;
    }

    public void linkPlayerMatch(int playerId, int matchId) throws PersistenceException {
        LOG.trace("Called - linkPlayerMatch");
        try (Connection connection = jdbcConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER_IN_MATCH)) {

            ps.setInt(1, playerId);
            ps.setInt(2, matchId);

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Could not link match with player";
            LOG.error(msg, e);
            throw new PersistenceException(msg, e);
        }
    }

    public List<MatchDTO> readMatches() throws PersistenceException {
        LOG.trace("Called - readMatches");
        List<MatchDTO> result = new LinkedList<>();
        try (Connection connection = jdbcConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(READ_ALL_MATCHES, Statement.RETURN_GENERATED_KEYS)) {

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {

                    MatchDTO match = new MatchDTO();
                    match.setId(rs.getInt(1));
                    match.setDateTime(rs.getTimestamp(2).toLocalDateTime());
                    match.setTeamRedGoals(rs.getInt(3));
                    match.setTeamBlueGoals(rs.getInt(4));
                    match.setTeamSize(rs.getInt(5));

                    // retrieve the players from the match
                    List<MatchPlayerDTO> matchPlayers = readMatchPlayers(match.getId(), connection);
                    match.setPlayerData(matchPlayers);

                    result.add(match);
                    LOG.debug("Added match to the result list!");
                }
            } catch (SQLException e) {
                String msg = "Could not read resultSet of match";
                LOG.error(msg, e);
                throw new PersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not read match";
            LOG.error(msg, e);
            throw new PersistenceException(msg, e);
        }
        return result;
    }

    private List<MatchPlayerDTO> readMatchPlayers(int matchId, Connection connection) throws PersistenceException{
        LOG.trace("Called - readMatchPlayers");
        List<MatchPlayerDTO> result = new LinkedList<>();
        try (PreparedStatement ps1 = connection.prepareStatement(READ_ALLPLAYERS_FROM_MATCH, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement ps2 = connection.prepareStatement(READ_PLAYERS_FROM_MATCHES, Statement.RETURN_GENERATED_KEYS)) {

            // retrieve all rows from the playerInMatch table that matches matchId
            ps1.setInt(1, matchId);

            try (ResultSet rs1 = ps1.executeQuery()) {
                while(rs1.next()) {

                    // execute another query that receives the player from the matchPlayer table
                    ps2.setInt(1, rs1.getInt(1));

                    try (ResultSet rs2 = ps2.executeQuery()) {
                        rs2.next();
                        MatchPlayerDTO matchPlayer = new MatchPlayerDTO();
                        matchPlayer.setId(rs2.getInt(1));
                        matchPlayer.setName(rs2.getString(2));
                        matchPlayer.setTeam(rs2.getInt(3));
                        matchPlayer.setScore(rs2.getInt(4));
                        matchPlayer.setGoals(rs2.getInt(5));
                        matchPlayer.setAssists(rs2.getInt(6));
                        matchPlayer.setSaves(rs2.getInt(7));
                        matchPlayer.setShots(rs2.getInt(8));

                        result.add(matchPlayer);
                    } catch (SQLException e) {
                        String msg = "Could not read resultSet of match players";
                        LOG.error(msg, e);
                        throw new PersistenceException(msg, e);
                    }
                }
            } catch (SQLException e) {
                String msg = "Could not read resultSet of players in matches";
                LOG.error(msg, e);
                throw new PersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not read match players";
            LOG.error(msg, e);
            throw new PersistenceException(msg, e);
        }
        return result;
    }
}
