package at.ac.tuwien.sepm.assignment.group.replay.dao.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class JDBCMatchDAO implements MatchDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INSERT_MATCH = "INSERT INTO match_ SET dateTime = ?, teamSize = ?, readId = ?," +
        " timeBallInBlueSide = ?, timeBallInRedSide = ?, possessionBlue = ?, possessionRed = ?, ballHeatmapFilename = ?, fileName = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET  playerid = ?, matchid = ?, name = ?," +
        " team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?, airTime = ?, groundTime = ?, homeSideTime = ?, " +
        "enemySideTime = ?, averageSpeed = ?, averageDistanceToBall = ?, heatmapFilename = ?";

    private static final String INSERT_BOOSTPAD_INFO = "INSERT INTO matchPlayerBoostPads SET playerid = ?, matchid = ?," +
        "boostpad67 = ?, boostpad12 = ?, boostpad43 = ?, boostpad13 = ?, boostpad66 = ?, boostpad18 = ?," +
        "  boostpad11 = ?, boostpad17 = ?, boostpad5 = ?, boostpad14 = ?, boostpad4 = ?, boostpad10 = ?," +
        "  boostpad7 = ?, boostpad41 = ?, boostpad3 = ?, boostpad64 = ?, boostpad40 = ?, boostpad42 = ?," +
        "  boostpad63 = ?, boostpad23 = ?, boostpad19 = ?, boostpad20 = ?, boostpad31 = ?, boostpad28 = ?," +
        "  boostpad21 = ?, boostpad36 = ?, boostpad68 = ?, boostpad32 = ?, boostpad38 = ?, boostpad34 = ?," +
        "  boostpad35 = ?, boostpad33 = ?, boostpad65 = ?, boostpad39 = ?";

    private static final String READ_ALL_MATCHES = "SELECT * FROM match_";
    private static final String READ_PLAYERS_FROM_MATCHES = "SELECT * FROM matchPlayer WHERE matchid = ?";

    private static final String READ_MATCH_BY_READID = "Select id from match_ where readId = ?";

    private static final String DELETE_MATCH = "Delete from match_ where id = ?";

    private final Connection connection;

    private PlayerDAO playerDAO;
    private FolderDAO folderDAO;

    public JDBCMatchDAO(JDBCConnectionManager jdbcConnectionManager, PlayerDAO playerDAO, FolderDAO folderDAO) throws SQLException {
        this.connection = jdbcConnectionManager.getConnection();
        this.playerDAO = playerDAO;
        this.folderDAO = folderDAO;
    }

    @Override
    public void createMatch(MatchDTO matchDTO) throws MatchPersistenceException, MatchAlreadyExistsException {
        LOG.trace("Called - createMatch");
        folderDAO.saveHeatmaps(matchDTO);
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
            ps.setDouble(4, matchDTO.getTimeBallInBlueSide());
            ps.setDouble(5, matchDTO.getTimeBallInRedSide());
            ps.setInt(6, matchDTO.getPossessionBlue());
            ps.setInt(7, matchDTO.getPossessionRed());
            ps.setString(9, matchDTO.getReplayFile().getName());
            ps.setString(8, matchDTO.getBallHeatmapFilename());
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
            ps.setDouble(10,matchPlayerDTO.getAirTime());
            ps.setDouble(11,matchPlayerDTO.getGroundTime());
            ps.setDouble(12,matchPlayerDTO.getHomeSideTime());
            ps.setDouble(13,matchPlayerDTO.getEnemySideTime());
            ps.setDouble(14,matchPlayerDTO.getAverageSpeed());
            ps.setDouble(15, matchPlayerDTO.getAverageDistanceToBall());
            ps.setString(16, matchPlayerDTO.getHeatmapFilename());

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Could not create matchPlayer";
            throw new MatchPersistenceException(msg, e);
        }

        // create boost pad record for the match player
        createBoostPadList(matchPlayerDTO, matchPlayerDTO.getBoostPadMap());
    }

    /**
     * Creates a record for the boost pad information for each match player
     * @param matchPlayerDTO the match player to store boost pad information for.
     * @param boostPadDTOMap the boost pad information map belonging to the match player dto.
     */
    private void createBoostPadList(MatchPlayerDTO matchPlayerDTO, Map<Integer, List<BoostPadDTO>> boostPadDTOMap) throws MatchPersistenceException{
        try(PreparedStatement ps = connection.prepareStatement(INSERT_BOOSTPAD_INFO)) {

            ps.setLong(1, matchPlayerDTO.getPlayerId());
            ps.setInt(2, matchPlayerDTO.getMatchId());
            ps.setInt(3, boostPadDTOMap.get(0).size());
            ps.setInt(4, boostPadDTOMap.get(1).size());
            ps.setInt(5, boostPadDTOMap.get(2).size());
            ps.setInt(6, boostPadDTOMap.get(3).size());
            ps.setInt(7, boostPadDTOMap.get(4).size());
            ps.setInt(8, boostPadDTOMap.get(5).size());
            ps.setInt(9, boostPadDTOMap.get(6).size());
            ps.setInt(10, boostPadDTOMap.get(7).size());
            ps.setInt(11, boostPadDTOMap.get(8).size());
            ps.setInt(12, boostPadDTOMap.get(9).size());
            ps.setInt(13, boostPadDTOMap.get(10).size());
            ps.setInt(14, boostPadDTOMap.get(11).size());
            ps.setInt(15, boostPadDTOMap.get(12).size());
            ps.setInt(16, boostPadDTOMap.get(13).size());
            ps.setInt(17, boostPadDTOMap.get(14).size());
            ps.setInt(18, boostPadDTOMap.get(15).size());
            ps.setInt(19, boostPadDTOMap.get(16).size());
            ps.setInt(20, boostPadDTOMap.get(17).size());
            ps.setInt(21, boostPadDTOMap.get(18).size());
            ps.setInt(22, boostPadDTOMap.get(19).size());
            ps.setInt(23, boostPadDTOMap.get(20).size());
            ps.setInt(24, boostPadDTOMap.get(21).size());
            ps.setInt(25, boostPadDTOMap.get(22).size());
            ps.setInt(26, boostPadDTOMap.get(23).size());
            ps.setInt(27, boostPadDTOMap.get(24).size());
            ps.setInt(28, boostPadDTOMap.get(25).size());
            ps.setInt(29, boostPadDTOMap.get(26).size());
            ps.setInt(30, boostPadDTOMap.get(27).size());
            ps.setInt(31, boostPadDTOMap.get(28).size());
            ps.setInt(32, boostPadDTOMap.get(29).size());
            ps.setInt(33, boostPadDTOMap.get(30).size());
            ps.setInt(34, boostPadDTOMap.get(31).size());
            ps.setInt(35, boostPadDTOMap.get(32).size());
            ps.setInt(36, boostPadDTOMap.get(33).size());

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Could not record boost pad information";
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
                    match.setPossessionBlue(rs.getInt("possessionBlue"));
                    match.setPossessionRed(rs.getInt("possessionRed"));
                    match.setTimeBallInBlueSide(rs.getDouble("timeBallInBlueSide"));
                    match.setTimeBallInRedSide(rs.getDouble("timeBallInRedSide"));
                    match.setReplayFile(folderDAO.getFile(rs.getString("fileName")));
                    //match.setReplayFile(new File(rs.getString("fileName")));
                    match.setBallHeatmapFilename(rs.getString("ballHeatmapFilename"));

                    // retrieve the players from the match
                    List<MatchPlayerDTO> matchPlayers = readMatchPlayers(match);
                    match.setPlayerData(matchPlayers);
                    folderDAO.getHeatmaps(match);
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
                    matchPlayer.setAirTime(rs.getDouble("airTime"));
                    matchPlayer.setGroundTime(rs.getDouble("groundTime"));
                    matchPlayer.setHomeSideTime(rs.getDouble("homeSideTime"));
                    matchPlayer.setEnemySideTime(rs.getDouble("enemySideTime"));
                    matchPlayer.setAverageSpeed(rs.getDouble("averageSpeed"));
                    matchPlayer.setAverageDistanceToBall(rs.getDouble("averageDistanceToBall"));
                    matchPlayer.setHeatmapFilename(rs.getString("heatmapFilename"));

                    result.add(matchPlayer);
                }
            }
        } catch (SQLException e) {
            String msg = "Could not read match players";
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }

    @Override
    public void deleteMatch(MatchDTO matchDTO) throws MatchPersistenceException {
        LOG.trace("Called - deleteMatch");
        try (PreparedStatement ps = connection.prepareStatement(DELETE_MATCH)) {
            ps.setInt(1, matchDTO.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new MatchPersistenceException("Could not delete match", e);
        }
    }
}
