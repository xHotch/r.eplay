package at.ac.tuwien.sepm.assignment.group.replay.dao.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dao.MatchDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.PlayerDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.TeamDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.PlayerPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.TeamPersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.util.JDBCConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class JDBCTeamDAO implements TeamDAO {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String INSERT_TEAM = "INSERT INTO team SET name = ?, teamSize = ?";
    private static final String INSERT_TEAMPLAYER = "INSERT INTO teamPlayer SET teamId = ?, playerId = ?";
    private static final String DELETE_TEAM = "Delete from team where id = ?";
    private static final String DELETE_TEAMPLAYER = "Delete from teamPlayer where teamId = ? and playerId = ?";

    private static final String READ_ALL_TEAMS = "SELECT * FROM team";
    private static final String READ_PLAYERS_FROM_TEAM = "SELECT * from teamPlayer where teamId = ?";

    private static final String READ_MATCHES_FROM_TEAMS = "Select *" +
        "from MATCH_ where ID = any( " +
        "SELECT mp.MATCHID " +
        "from MATCHPLAYER mp join TEAMPLAYER tp join team t on tp.TEAMID = t.ID on mp.PLAYERID = tp.PLAYERID " +
        "where t.ID = ? group by mp.MATCHID having count(mp.PLAYERID) = t.TEAMSIZE and sum(mp.TEAM) in (0 ,t.TEAMSIZE) " +
        "INTERSECT SELECT mp.MATCHID " +
        "from MATCHPLAYER mp join TEAMPLAYER tp join team t on tp.TEAMID = t.ID on mp.PLAYERID = tp.PLAYERID " +
        "where t.ID = ? group by mp.MATCHID having count(mp.PLAYERID) = t.TEAMSIZE and sum(mp.TEAM) in (0 ,t.TEAMSIZE) )";

    private final Connection connection;

    private PlayerDAO playerDAO;
    private JDBCMatchDAO matchDAO;

    public JDBCTeamDAO(JDBCConnectionManager jdbcConnectionManager, PlayerDAO playerDAO, JDBCMatchDAO matchDAO) throws SQLException {
        this.connection = jdbcConnectionManager.getConnection();
        this.playerDAO = playerDAO;
        this.matchDAO = matchDAO;
    }

    @Override
    public void createTeam(TeamDTO teamDTO) throws TeamPersistenceException {
        LOG.trace("Called - createTeam");

        try (PreparedStatement ps = connection.prepareStatement(INSERT_TEAM, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, teamDTO.getName());
            ps.setInt(2, teamDTO.getTeamSize());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                teamDTO.setId(rs.getLong("id"));
            }
            LOG.debug("Inserted team with id {}", teamDTO.getId());
        }  catch (SQLException e) {
            String msg = "Could not create team";
            throw new TeamPersistenceException(msg, e);
        }

        for (PlayerDTO playerDTO : teamDTO.getPlayers()) {
            try (PreparedStatement ps = connection.prepareStatement(INSERT_TEAMPLAYER)) {
                ps.setLong(1, teamDTO.getId());
                ps.setLong(2, playerDTO.getId());

                ps.executeUpdate();
                LOG.debug("Inserted a Teamplyer with player id: {} and team id: {}",playerDTO.getId(), teamDTO.getId());
            } catch (SQLException e) {
                String msg = "Could not add player to team";
                throw new TeamPersistenceException(msg, e);
            }
        }
    }

    @Override
    public void deleteTeam(TeamDTO teamDTO) throws TeamPersistenceException {
        LOG.trace("Called - deleteTeam");

        try (PreparedStatement ps = connection.prepareStatement(DELETE_TEAM)) {
            ps.setLong(1, teamDTO.getId());

            ps.executeUpdate();
        }  catch (SQLException e) {
            String msg = "Could not delete team";
            throw new TeamPersistenceException(msg, e);
        }

        for (PlayerDTO playerDTO : teamDTO.getPlayers()) {
            try (PreparedStatement ps = connection.prepareStatement(DELETE_TEAMPLAYER)) {
                ps.setLong(1, teamDTO.getId());
                ps.setLong(2, playerDTO.getId());

                ps.executeUpdate();
            } catch (SQLException e) {
                String msg = "Could not delete player from team";
                throw new TeamPersistenceException(msg, e);
            }
        }
    }

    @Override
    public List<TeamDTO> readTeams() throws TeamPersistenceException {
        LOG.trace("Called - readTeams");
        List<TeamDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_ALL_TEAMS, Statement.RETURN_GENERATED_KEYS)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeamDTO teamDTO = new TeamDTO();
                    teamDTO.setId(rs.getLong("id"));
                    teamDTO.setTeamSize(rs.getInt("teamSize"));
                    teamDTO.setName(rs.getString("name"));

                    List<PlayerDTO> players = readTeamPlayers(teamDTO);
                    teamDTO.setPlayers(players);
                    result.add(teamDTO);
                }
            }
        } catch (SQLException e) {
            throw new TeamPersistenceException("could not read teams", e);
        }
        return result;
    }

    public List<MatchDTO> readTeamMatches(TeamDTO teamDTO1, TeamDTO teamDTO2) throws TeamPersistenceException {
        LOG.trace("Called - readTeamMatches");
       List<MatchDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_MATCHES_FROM_TEAMS)) {
            ps.setLong(1, teamDTO1.getId());
            ps.setLong(2, teamDTO2.getId());
            matchDAO.setMatchDTO(result,ps);
        } catch (SQLException | MatchPersistenceException | FilePersistenceException e) {
            throw new TeamPersistenceException("could not read team Matches", e);
        }
        return result;
    }

    private List<PlayerDTO> readTeamPlayers(TeamDTO teamDTO) throws TeamPersistenceException {
        LOG.trace("Called - readTeamPlayers");
        List<PlayerDTO> result = new LinkedList<>();
        try (PreparedStatement ps = connection.prepareStatement(READ_PLAYERS_FROM_TEAM)) {
            ps.setLong(1, teamDTO.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PlayerDTO playerDTO = playerDAO.get(rs.getInt("playerId"));
                    result.add(playerDTO);
                }
            }
        }catch (PlayerPersistenceException e) {
            throw new TeamPersistenceException("could not read player", e);
        } catch (SQLException e) {
            throw new TeamPersistenceException("could not read players", e);
        }
        return result;
    }
}
