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
    private static final String INSERT_MATCH = "INSERT INTO match_ SET dateTime = ?, teamBlueGoals = ?, teamRedGoals = ?, teamSize = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET name = ?, team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?";
    private static final String INSERT_PLAYER_IN_MATCH = "INSERT INTO playerInMatch SET playerid = ?, matchid = ?";

    private static final String READ_ALL_MATCHES = "SELECT * FROM match_";
    private static final String READ_PLAYERS_FROM_MATCHES = "SELECT * FROM matchPlayer NATURAL JOIN playerInMatch WHERE matchid = ?";

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
                throw new MatchPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not create match";
            LOG.error(msg, e);
            throw new MatchPersistenceException(msg, e);
        }
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
            int playerID;
            playerID = createMatchPlayer(matchPlayerDTO);
            linkPlayerMatch(playerID,matchID);
        }
    }

    private int createMatchPlayer(MatchPlayerDTO matchPlayerDTO) throws MatchPersistenceException {
        LOG.trace("Called - createMatchPlayer");
        int matchPlayerID;
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MATCH_PLAYER, Statement.RETURN_GENERATED_KEYS)) {

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
                throw new MatchPersistenceException(msg, e);
            }
        } catch (SQLException e) {
            String msg = "Could not create matchPlayer";
            LOG.error(msg, e);
            throw new MatchPersistenceException(msg, e);
        }
        return matchPlayerID;
    }

    public void linkPlayerMatch(int playerId, int matchId) throws MatchPersistenceException {
        LOG.trace("Called - linkPlayerMatch");
        try (PreparedStatement ps = connection.prepareStatement(INSERT_PLAYER_IN_MATCH)) {

            ps.setInt(1, playerId);
            ps.setInt(2, matchId);

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Could not link match with player";
            LOG.error(msg, e);
            throw new MatchPersistenceException(msg, e);
        }
    }

    public List<MatchDTO> readMatches() throws MatchPersistenceException {
        LOG.trace("Called - readMatches");
        List<MatchDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_ALL_MATCHES, Statement.RETURN_GENERATED_KEYS)) {

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {

                    MatchDTO match = new MatchDTO();
                    match.setId(rs.getInt("id"));
                    match.setDateTime(rs.getTimestamp("dateTime").toLocalDateTime());
                    match.setTeamRedGoals(rs.getInt("teamRedGoals"));
                    match.setTeamBlueGoals(rs.getInt("teamBlueGoals"));
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
                    matchPlayer.setId(rs.getInt("id"));
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
