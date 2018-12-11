package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
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
public class CarInformationParser {
    private int actorId;
    private int currentFrame;
    private int currentActorUpdateNr;
    private double frameTime;
    private double frameDelta;
    private boolean gamePaused;

    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;

    public void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    public CarInformationParser(RigidBodyParser rigidBodyParser) {
        this.rigidBodyParser = rigidBodyParser;
    }

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    //Map that maps ActorID from a Player to a car. Key = CarActorId, Value = playerActorId
    private Map<Integer, Integer> playerCarMap = new HashMap<>();


    //Map that maps ActorID from a Car to a list of RigidBodyInformation
    private Map<Integer, List<RigidBodyInformation>> rigidBodyMap = new HashMap<>();


    void parse(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {


        this.actorId = actorId;
        this.currentFrame = currentFrame;
        this.currentActorUpdateNr = currentActorUpdateNr;
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.gamePaused = gamePaused;

        getPlayerIDfromCar();
        parseRigidBodyInformation();

    }

    private void getPlayerIDfromCar() {
        LOG.trace("Called - getPlayerIDfromCar");
        try {
            playerCarMap.putIfAbsent(actorId, ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['Engine.Pawn:PlayerReplicationInfo'].ActorId"));
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about player found");
        }
    }

    private void parseRigidBodyInformation() throws FileServiceException {
        LOG.trace("Called - parseRigidBodyInformation");

        rigidBodyMap.putIfAbsent(actorId, new ArrayList<>());
        try {
            rigidBodyMap.get(actorId).add(rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused));
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about player found");
        }
    }
}
