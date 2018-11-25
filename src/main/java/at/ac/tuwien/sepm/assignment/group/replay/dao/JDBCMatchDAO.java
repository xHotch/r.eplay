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

@Component
public class JDBCMatchDAO implements MatchDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INSERT_MATCH = "INSERT INTO match SET dateTime = ?, teamBlueGoals = ?, teamRedGoals = ?, teamSize = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET name = ?, team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?";
    private static final String INSERT_PLAYER_IN_MATCH = "INSERT INTO playerInMatch SET playerid = ?, matchid = ?";

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
             PreparedStatement ps = connection.prepareStatement(INSERT_MATCH_PLAYER)) {

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


}
