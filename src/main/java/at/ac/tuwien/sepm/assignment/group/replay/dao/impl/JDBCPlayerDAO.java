package at.ac.tuwien.sepm.assignment.group.replay.dao.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.AvgStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
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
    private static final String INSERT_PLAYER = "INSERT INTO player SET name = ?, plattformid = ?, shown = ?";

    private static final String READ_ALL_PLAYERS = "SELECT * FROM player WHERE shown = true";

    private static final String GET_PLAYER = "SELECT * FROM player WHERE id = ?";

    private static final String SET_SHOW_PLAYER = "UPDATE player SET shown = ? WHERE id = ?";

    private static final String READ_PLAYER_BY_PLATFORMID = "Select id from player where plattformid = ?";

    private static final String GET_AVG_STATS = "Select avg(Cast(mp.score as float)), avg(Cast(mp.goals as float)), " +
        "avg(Cast(mp.assists as float)), avg(cast(mp.saves as float)), avg(cast(mp.shots as float)), avg(mp.averageSpeed) " +
        "from matchPlayer mp join match_ m on mp.matchid = m.id where mp.playerid = ? and m.teamSize = ?";

    private final Connection connection;

    public JDBCPlayerDAO(JDBCConnectionManager jdbcConnectionManager) throws SQLException {
        this.connection = jdbcConnectionManager.getConnection();
    }


    @Override
    public long createPlayer(PlayerDTO playerDTO) throws PlayerPersistenceException {
        LOG.trace("Called - createPlayer");

        try (PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement ps2 = connection.prepareStatement(READ_PLAYER_BY_PLATFORMID)) {
            ps2.setLong(1, playerDTO.getPlatformID());
            try (ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    playerDTO.setId(rs2.getInt("id"));
                    return playerDTO.getId();
                }
            }
            ps.setString(1, playerDTO.getName());
            ps.setLong(2, playerDTO.getPlatformID());
            ps.setBoolean(3, playerDTO.isShown());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                playerDTO.setId(rs.getLong("id"));
                return playerDTO.getId();
            }
        } catch (SQLException e) {
            throw new PlayerPersistenceException("Could not create player", e);
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
                    setPlayerDTO(rs, player);

                    result.add(player);
                    LOG.debug("Added player to the result list!");
                }
            }
        } catch (SQLException e) {
            throw new PlayerPersistenceException("Could not read player", e);
        }
        return result;
    }

    private void setPlayerDTO(ResultSet rs, PlayerDTO player) throws SQLException {
        player.setId(rs.getInt("id"));
        player.setName(rs.getString("name"));
        player.setPlatformID(rs.getLong("plattformid"));
        player.setShown(rs.getBoolean("shown"));
    }

    @Override
    public void deletePlayer(PlayerDTO playerToDelete) throws PlayerPersistenceException {
        LOG.trace("Called - deletePlayer");

        try (PreparedStatement ps = connection.prepareStatement(SET_SHOW_PLAYER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBoolean(1,false);
            ps.setLong(2,playerToDelete.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new PlayerPersistenceException("Could not delete player", e);
        }
    }

    @Override
    public void showPlayer(PlayerDTO playerDTO) throws PlayerPersistenceException {
        LOG.trace("Called - showPlayer");

        try (PreparedStatement ps = connection.prepareStatement(SET_SHOW_PLAYER, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBoolean(1,true);
            ps.setLong(2, playerDTO.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new PlayerPersistenceException("Could not update player to add it to the list of shown players", e);
        }

    }

    @Override
    public PlayerDTO get(int id) throws PlayerPersistenceException{
        LOG.trace("Called - showPlayer");
        PlayerDTO player;
        try (PreparedStatement ps = connection.prepareStatement(GET_PLAYER, Statement.RETURN_GENERATED_KEYS)){

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();

                player = new PlayerDTO();
                setPlayerDTO(rs, player);

                LOG.debug("Added player to the result list!");

                return player;
            }
        } catch (SQLException e) {
            throw new PlayerPersistenceException("Could not get player", e);
        }

    }

    @Override
    public AvgStatsDTO getAvgStats(PlayerDTO playerDTO, MatchType matchType) throws PlayerPersistenceException {
        LOG.trace("Called - getAvgStats");
        AvgStatsDTO avgStatsDTO = new AvgStatsDTO();

        try (PreparedStatement ps = connection.prepareStatement(GET_AVG_STATS, Statement.RETURN_GENERATED_KEYS)){
            ps.setLong(1, playerDTO.getId());
            if (matchType == MatchType.RANKED1V1) {
                ps.setInt(2, 1);
            } else if (matchType == MatchType.RANKED2V2) {
                ps.setInt(2, 2);
            } else {
                ps.setInt(2, 3);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                avgStatsDTO.setScore(rs.getDouble(1));
                avgStatsDTO.setGoals(rs.getDouble(2));
                avgStatsDTO.setAssists(rs.getDouble(3));
                avgStatsDTO.setSaves(rs.getDouble(4));
                avgStatsDTO.setShots(rs.getDouble(5));
                avgStatsDTO.setSpeed(rs.getDouble(6));
            }
        } catch (SQLException e) {
            throw new PlayerPersistenceException("Could not get avg statistics for player", e);
        }

        return avgStatsDTO;
    }
}
