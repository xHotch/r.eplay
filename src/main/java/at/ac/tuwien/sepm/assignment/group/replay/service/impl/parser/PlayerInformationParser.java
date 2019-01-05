package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerInformationParser {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ReadContext ctx;

    private Map<Long, Integer> platformIdToActorId = new HashMap<>();

    void setUp() {
        platformIdToActorId = new HashMap<>();
    }

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    void parse(int actorId, int currentFrame, int currentActorUpdateNr) throws FileServiceException {
        LOG.trace("called - parse");
        String actorUpdate = "].ActorUpdates[";
        String frame = "$.Frames[";
        if (ctx.read(frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:UniqueId']") != null) {
            Long uniqueId = ctx.read(frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:UniqueId'].SteamID64", Long.class);
            if (uniqueId != null) {
                platformIdToActorId.put(uniqueId, actorId);
            } else {
                uniqueId = ctx.read(frame + currentFrame + actorUpdate + currentActorUpdateNr + "].['Engine.PlayerReplicationInfo:UniqueId'].PsnId", Long.class);
                if (uniqueId != null) {
                    platformIdToActorId.put(uniqueId, actorId);
                } else {
                    throw new FileServiceException("Actor has no steam-id and no psn-id");
                }
            }
        }
    }

    void setActorId(List<MatchPlayerDTO> players) throws FileServiceException {
        LOG.trace("called - setActorId");
        for (MatchPlayerDTO dto : players) {
            if (platformIdToActorId.get(dto.getPlayerDTO().getPlatformID()) != null) {
                dto.setActorId(platformIdToActorId.get(dto.getPlayerDTO().getPlatformID()));
            } else {
                throw new FileServiceException("No ActorId for a player");
            }
        }
    }

    public Map<Long, Integer> getPlatformIdToActorId() {
        return platformIdToActorId;
    }

    public void setPlatformIdToActorId(Map<Long, Integer> platformIdToActorId) {
        this.platformIdToActorId = platformIdToActorId;
    }
}
