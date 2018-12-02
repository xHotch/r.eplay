package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Gabriel Aichinger
 */
@Component
public class JDBCPlayerDAO implements PlayerDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INSERT_PLAYER = "INSERT INTO player SET name = ?, plattformid = ?";

    private static final String READ_ALL_PLAYERS = "SELECT * FROM player";

    private final Connection connection;

    public JDBCPlayerDAO(JDBCConnectionManager jdbcConnectionManager) {
        this.connection = jdbcConnectionManager.getConnection();
    }


    @Override
    public void createPlayer(PlayerDTO playerDTO) throws PlayerPersistenceException {
        LOG.trace("Called - createPlayer");

        try (PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playerDTO.getName());
            ps.setLong(2, playerDTO.getPlattformid());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                playerDTO.setId(rs.getInt("id"));
            } catch (SQLException e) {
                String msg = "Could not read resultSet of player";
                LOG.error(msg, e);
                throw new PlayerPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not create player";
            LOG.error(msg, e);
            throw new PlayerPersistenceException(msg, e);
        }

    }

    @Override
    public List<PlayerDTO> readPlayers() throws PlayerPersistenceException {
        LOG.trace("Called - readPlayers");
        List<PlayerDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_ALL_PLAYERS, Statement.RETURN_GENERATED_KEYS)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    PlayerDTO player = new PlayerDTO();
                    player.setId(rs.getInt("id"));
                    player.setName(rs.getString("name"));
                    player.setPlattformid(rs.getLong("plattformid"));

                    result.add(player);
                    LOG.debug("Added player to the result list!");
                }
            } catch (SQLException e) {
                String msg = "Could not read resultSet of player";
                LOG.error(msg, e);
                throw new PlayerPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not read player";
            LOG.error(msg, e);
            throw new PlayerPersistenceException(msg, e);
        }
        return result;
    }
}
