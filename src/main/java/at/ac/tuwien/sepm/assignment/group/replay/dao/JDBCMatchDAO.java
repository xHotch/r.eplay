package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
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

    private final JDBCConnectionManager jdbcConnectionManager;

    public JDBCMatchDAO(JDBCConnectionManager jdbcConnectionManager) {
        this.jdbcConnectionManager = jdbcConnectionManager;
    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws PersistenceException {
        LOG.trace("Called - createMatch");
        try (Connection connection = jdbcConnectionManager.getConnection(); PreparedStatement ps = connection.prepareStatement(INSERT_MATCH)) {

            ps.setTimestamp(1, Timestamp.valueOf(matchDTO.getDateTime()));
            ps.setInt(2, matchDTO.getTeamBlueGoals());
            ps.setInt(2, matchDTO.getTeamRedGoals());
            ps.setInt(3, matchDTO.getTeamSize());

        } catch (SQLException e) {
            LOG.error("Could not execute database query", e);
            throw new PersistenceException("Could not execute database query", e);
        }
    }
}
