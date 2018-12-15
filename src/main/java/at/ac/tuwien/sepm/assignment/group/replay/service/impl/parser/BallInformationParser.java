package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class BallInformationParser {


    private int currentFrame;
    private int currentActorUpdateNr;
    private double frameTime;
    private double frameDelta;
    private boolean gamePaused;

    private EnumMap<TeamSide, Integer> hitCount;

    private ArrayList<RigidBodyInformation> rigidBodyInformations = new ArrayList<>();

    public ArrayList<RigidBodyInformation> getRigidBodyInformations() {
        return rigidBodyInformations;
    }

    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    public BallInformationParser(RigidBodyParser rigidBodyParser) {
        this.rigidBodyParser = rigidBodyParser;
    }

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    void setup(){
        rigidBodyInformations=new ArrayList<>();
        hitCount = new EnumMap<>(TeamSide.class);
    }

    void parse(int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {
        LOG.trace("Called - parse");

        this.currentFrame = currentFrame;
        this.currentActorUpdateNr = currentActorUpdateNr;
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.gamePaused = gamePaused;

        rigidBodyInformations.add(rigidBodyParser.parseRigidBodyInformation(this.currentFrame, this.currentActorUpdateNr, this.frameTime, this.frameDelta, this.gamePaused));
        parseHitInformation();
    }

    private void parseHitInformation(){
        try {
            int hit = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.Ball_TA:HitTeamNum']",Integer.class);

            TeamSide side = TeamSide.getById(hit).get();
            hitCount.put(side,hitCount.getOrDefault(side,0)+1);
            //todo save time

        } catch (NullPointerException e){
            LOG.debug("No Hit Information found");
        }

    }

    public EnumMap<TeamSide, Integer> getHitCount() {
        return hitCount;
    }
}