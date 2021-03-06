package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.FrameDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class BallInformationParser {
    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Map<TeamSide, Integer> hitCount;

    private Map<Double, TeamSide> hitTimes;

    private List<RigidBodyInformation> rigidBodyInformations = new ArrayList<>();

    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;


    public BallInformationParser(RigidBodyParser rigidBodyParser) {
        this.rigidBodyParser = rigidBodyParser;
    }

    List<RigidBodyInformation> getRigidBodyInformations() {
        return rigidBodyInformations;
    }

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    void setup(){
        rigidBodyInformations=new ArrayList<>();
        hitCount = new EnumMap<>(TeamSide.class);
        hitTimes = new TreeMap<>();
    }

    void parse(int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) {
        LOG.trace("Called - parse");


        rigidBodyInformations.add(rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime,  gamePaused));
        parseHitInformation(currentFrame, currentActorUpdateNr, frameTime);
    }

    void parseVideoFrame(int currentFrame, int currentActorUpdateNr, FrameDTO frameDTO, boolean gamePaused, double frameTime) {
        LOG.trace("Called - parse");


        frameDTO.setBallRigidBodyInformation(rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime,  gamePaused));
    }

    private void parseHitInformation(int currentFrame, int currentActorUpdateNr, double frameTime){
        try {
            int hit = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.Ball_TA:HitTeamNum']",Integer.class);

            TeamSide side = TeamSide.getById(hit).get();
            hitCount.put(side,hitCount.getOrDefault(side,0)+1);
            hitTimes.put(frameTime, side);

        } catch (NullPointerException e){
            LOG.debug("No Hit Information found");
        }
    }

    public Map<TeamSide, Integer> getHitCount() {
        return hitCount;
    }

    Map<Double, TeamSide> getHitTimes() {
        return hitTimes;
    }
}