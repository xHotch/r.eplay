package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerInformationParser {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ReadContext ctx;

    private Map<Long, Integer> platformIdToActorId = new HashMap<>();
    private Map<Integer, TeamSide> actorIdToTeam = new HashMap<>();

    private Map<Integer, MatchPlayerDTO> matchPlayers = new HashMap<>();

    void setUp() {
        platformIdToActorId = new HashMap<>();
        actorIdToTeam = new HashMap<>();
        matchPlayers = new HashMap<>();
    }

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    void parse(int actorId, int currentFrame, int currentActorUpdateNr) throws FileServiceException {
        LOG.trace("called - parse");
        String actorUpdate = "].ActorUpdates[";
        String frame = "$.Frames[";
        if (ctx.read(frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:UniqueId']") != null) {
            MatchPlayerDTO matchPlayer = new MatchPlayerDTO();
            PlayerDTO player = new PlayerDTO();
            matchPlayer.setPlayerDTO(player);
            Long uniqueId = ctx.read(frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:UniqueId'].SteamID64", Long.class);
            if (uniqueId != null) {

                platformIdToActorId.putIfAbsent(uniqueId, actorId);
                player.setPlatformID(uniqueId);
            } else {
                uniqueId = ctx.read(frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:UniqueId'].PsnId", Long.class);
                if (uniqueId != null) {
                    platformIdToActorId.putIfAbsent(uniqueId, actorId);
                    player.setPlatformID(uniqueId);
                } else {
                    throw new FileServiceException("Actor has no steam-id and no psn-id");
                }
            }
            try {
                String nameJSON = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:PlayerName']";
                String teamJSON = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:Team'].ActorId";
                if (ctx.read(nameJSON) != null) {
                    player.setName(ctx.read(nameJSON, String.class));
                } else {
                    throw new FileServiceException("Player has no name");
                }
                if (ctx.read(teamJSON) != null) {
                    matchPlayer.setTeamActorId(ctx.read(teamJSON, Integer.class));
                } else {
                    throw new FileServiceException("Player has no team");
                }
                matchPlayer.setActorId(actorId);
                matchPlayers.put(actorId, matchPlayer);
            } catch (PathNotFoundException e){
                throw new FileServiceException("Replay with reconnected player");
                //LOG.debug("No Path found while parsing PlayerInformation");
            }
        } else {
            MatchPlayerDTO matchPlayer = matchPlayers.get(actorId);
            String matchScore = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['TAGame.PRI_TA:MatchScore']";
            String matchShots = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['TAGame.PRI_TA:MatchShots']";
            String matchGoals = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['TAGame.PRI_TA:MatchGoals']";
            String matchSaves = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['TAGame.PRI_TA:MatchSaves']";
            String matchAssists = frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['TAGame.PRI_TA:MatchAssists']";
            if (ctx.read(matchScore, Integer.class) != null) {
                matchPlayer.setScore(ctx.read(matchScore, Integer.class));
            }
            if (ctx.read(matchShots, Integer.class) != null) {
                matchPlayer.setShots(ctx.read(matchShots, Integer.class));
            }
            if (ctx.read(matchGoals, Integer.class) != null) {
                matchPlayer.setGoals(ctx.read(matchGoals, Integer.class));
            }
            if (ctx.read(matchSaves, Integer.class) != null) {
                matchPlayer.setSaves(ctx.read(matchSaves, Integer.class));
            }
            if (ctx.read(matchAssists, Integer.class) != null) {
                matchPlayer.setAssists(ctx.read(matchAssists, Integer.class));
            }
        }
    }

    void parseTeam(int actorId, int currentFrame, int currentActorUpdateNr) {
        LOG.trace("called - parseTeam");
        String teamArchetypes = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].TypeName", String.class);
        if (teamArchetypes != null) {
            if (teamArchetypes.equals("Archetypes.Teams.Team1")) {
                actorIdToTeam.put(actorId, TeamSide.RED);
            } else {
                actorIdToTeam.put(actorId, TeamSide.BLUE);
            }
        }
    }

    List<MatchPlayerDTO> getMatchPlayer() {
        List<MatchPlayerDTO> list = new ArrayList<>();
        for (MatchPlayerDTO matchPlayerDTO : matchPlayers.values()) {
            matchPlayerDTO.setTeam(actorIdToTeam.get(matchPlayerDTO.getTeamActorId()));
            list.add(matchPlayerDTO);
        }
        return list;
    }

    Map<Long, Integer> getPlatformIdToActorId() {
        return platformIdToActorId;
    }

}
