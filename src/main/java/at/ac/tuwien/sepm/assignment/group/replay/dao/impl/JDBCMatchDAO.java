package at.ac.tuwien.sepm.assignment.group.replay.dao.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.FolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
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
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class JDBCMatchDAO implements MatchDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String INSERT_MATCH = "INSERT INTO match_ SET dateTime = ?, teamSize = ?, readId = ?," +
        " timeBallInBlueSide = ?, timeBallInRedSide = ?, possessionBlue = ?, possessionRed = ?, ballHeatmapFilename = ?, fileName = ?, matchTime = ?";
    private static final String INSERT_MATCH_PLAYER = "INSERT INTO matchPlayer SET  playerid = ?, matchid = ?, name = ?," +
        " team = ?, score = ?, goals = ?, assists = ?, saves = ?, shots = ?, airTime = ?, groundTime = ?, homeSideTime = ?, " +
        "enemySideTime = ?, averageSpeed = ?, averageDistanceToBall = ?, heatmapFilename = ?";

    private static final String INSERT_BOOSTPAD_INFO = "INSERT INTO matchPlayerBoostPads SET matchPlayerid = ?, matchid = ?," +
        "boostpad0 = ?, boostpad1 = ?, boostpad2 = ?, boostpad3 = ?, boostpad4 = ?, boostpad5 = ?," +
        "  boostpad6 = ?, boostpad7 = ?, boostpad8 = ?, boostpad9 = ?, boostpad10 = ?, boostpad11 = ?," +
        "  boostpad12 = ?, boostpad13 = ?, boostpad14 = ?, boostpad15 = ?, boostpad16 = ?, boostpad17 = ?," +
        "  boostpad18 = ?, boostpad19 = ?, boostpad20 = ?, boostpad21 = ?, boostpad22 = ?, boostpad23 = ?," +
        "  boostpad24 = ?, boostpad25 = ?, boostpad26 = ?, boostpad27 = ?, boostpad28 = ?, boostpad29 = ?," +
        "  boostpad30 = ?, boostpad31 = ?, boostpad32 = ?, boostpad33 = ?";

    private static final String SEARCH_MATCHES = "Select distinct m.* from match_ m join matchPlayer mp on m.id = mp.matchid where" +
        "(lower(mp.name) like lower(?) or ?) and (teamSize = ? or ?) and (dateTime <= ? or ?) and (dateTime >= ? or ?)";

    private static final String READ_ALL_MATCHES = "SELECT * FROM match_";
    private static final String READ_PLAYERS_FROM_MATCHES = "SELECT * FROM matchPlayer WHERE matchid = ?";
    private static final String READ_BOOSTPADLIST_FROM_MATCHPLAYER = "SELECT * FROM MATCHPLAYERBOOSTPADS WHERE matchplayerid = ? AND matchid = ?";

    private static final String READ_MATCHES_FROM_PLAYER = "SELECT m.* FROM match_ m join matchplayer mp on m.id = mp.matchid where mp.playerid = ?";

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
        try (PreparedStatement ps = connection.prepareStatement(INSERT_MATCH, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement ps2 = connection.prepareStatement(READ_MATCH_BY_READID)) {

            folderDAO.saveHeatmaps(matchDTO);

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
            ps.setString(9, matchDTO.getReplayFilename());
            ps.setString(8, matchDTO.getBallHeatmapFilename());
            ps.setDouble(10, matchDTO.getMatchTime());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                matchDTO.setId(rs.getInt("id"));
            }
        } catch (SQLException | FilePersistenceException e) {
            String msg = "Could not create match";
            throw new MatchPersistenceException(msg, e);
        }
        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {

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

            for(int i=3; i<=36; i++) {
                ps.setInt(i, boostPadDTOMap.get(i-3).size());
            }

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

            setMatchDTO(result, ps,true);
        } catch (SQLException | FilePersistenceException e) {
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

        for (MatchPlayerDTO matchPlayer:result) {
            Map<Integer, List<Integer>> dbBoostPadMap = new HashMap<>();
            //set boostpad map
            int playerID = (int)matchPlayer.getPlayerId();
            dbBoostPadMap.put(playerID, readBoostPadList(playerID, match.getId()));
            matchPlayer.setDBBoostPadMap(dbBoostPadMap);
        }
        return result;
    }

    /**
     * reads the boost pad records from the db
     * @param id player id from the corresponding match
     * @param matchId match id
     */
    private List<Integer> readBoostPadList(int id, int matchId) throws MatchPersistenceException{
        LOG.trace("Called - readBoostPadMap");
        List<Integer> boostPadList = new LinkedList<>();
        try (PreparedStatement ps2 = connection.prepareStatement(READ_BOOSTPADLIST_FROM_MATCHPLAYER, Statement.RETURN_GENERATED_KEYS)) {

            ps2.setInt(1, id);
            ps2.setInt(2, matchId);

            try (ResultSet rs2 = ps2.executeQuery()) {

                while (rs2.next()) {
                    for(int i=0; i<=33; i++) {
                        boostPadList.add(rs2.getInt("boostpad" + i));
                    }
                }
            }

        } catch (SQLException e) {
            String msg = "Could not read boost pads";
            throw new MatchPersistenceException(msg, e);
        }
        return boostPadList;
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

    @Override
    public List<MatchDTO> searchMatches(String name, LocalDateTime begin, LocalDateTime end, int teamSize) throws MatchPersistenceException {
        LOG.trace("Called - searchMatches");
        List<MatchDTO> result = new LinkedList<>();

        try (PreparedStatement ps = connection.prepareStatement(SEARCH_MATCHES, Statement.RETURN_GENERATED_KEYS)) {
            if (name == null) {
                ps.setString(1, "");
                ps.setBoolean(2, true);
            } else {
                ps.setString(1, "%" + name + "%");
                ps.setBoolean(2, false);
            }
            ps.setInt(3, teamSize);
            if (teamSize > 0 && teamSize <= 3) {
                ps.setBoolean(4, false);
            } else {
                ps.setBoolean(4, true);
            }
            if (end == null) {
                ps.setTimestamp(5, null);
                ps.setBoolean(6, true);
            } else {
                ps.setTimestamp(5, Timestamp.valueOf(end));
                ps.setBoolean(6, false);
            }

            if (begin == null) {
                ps.setTimestamp(7, null);
                ps.setBoolean(8, true);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(begin));
                ps.setBoolean(8, false);
            }
            setMatchDTO(result, ps,true);
        } catch (SQLException | FilePersistenceException e) {
            String msg = "Could not read match";
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }

    void setMatchDTO(List<MatchDTO> result, PreparedStatement ps,boolean files) throws SQLException, MatchPersistenceException, FilePersistenceException {
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
                match.setReplayFilename(rs.getString("fileName"));
                match.setBallHeatmapFilename(rs.getString("ballHeatmapFilename"));
                match.setMatchTime(rs.getDouble("matchTime"));

                // retrieve the players from the match
                List<MatchPlayerDTO> matchPlayers = readMatchPlayers(match);
                match.setPlayerData(matchPlayers);
                result.add(match);
                LOG.debug("Added match to the result list!");
            }
        }
    }

    @Override
    public List<MatchDTO> readMatchesFromPlayer(PlayerDTO playerDTO) throws MatchPersistenceException {
        LOG.trace("Called - readMatchesFromPlayer");
        List<MatchDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_MATCHES_FROM_PLAYER, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, playerDTO.getId());
            setMatchDTO(result,ps,false);
        } catch (SQLException | FilePersistenceException e) {
            String msg = "Could not read match";
            throw new MatchPersistenceException(msg, e);
        }
        return result;
    }
}
