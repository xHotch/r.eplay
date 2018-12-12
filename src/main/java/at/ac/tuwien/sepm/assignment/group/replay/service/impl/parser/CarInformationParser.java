package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class CarInformationParser {
    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private int actorId;
    private int currentFrame;
    private int currentActorUpdateNr;
    private double frameTime;
    private double frameDelta;
    private boolean gamePaused;

    //Map that maps ActorID from a Player to a car. Key = CarActorId, Value = playerActorId
    private LinkedHashMap<Integer, Integer> playerCarMap = new LinkedHashMap<>();

    //Map that maps ActorID from a Car to a list of RigidBodyInformation
    private LinkedHashMap<Integer, List<RigidBodyInformation>> rigidBodyMap = new LinkedHashMap<>();

    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;


    public CarInformationParser(RigidBodyParser rigidBodyParser) {
        this.rigidBodyParser = rigidBodyParser;
    }

    /**
     * Calls other methods, to read information about cars from the json file.
     *
     * @param currentFrame       ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime     frameTime of the frame
     * @param frameDelta    deltaTime of the frame
     * @param gamePaused    boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     * @throws FileServiceException if the file couldn't be parsed
     */
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

    /**
     * Reads the PlayerID of a player, if it is assigned to a car and puts it in the playerCarMap with Key = CarActorId, Value = playerActorId
     */
    private void getPlayerIDfromCar() {
        LOG.trace("Called - getPlayerIDfromCar");
        try {
            playerCarMap.putIfAbsent(actorId, ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['Engine.Pawn:PlayerReplicationInfo'].ActorId"));
            LOG.debug("New Player for car found at frame {}, actorupdate {}", currentFrame, currentActorUpdateNr);
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about player found");
        }
    }

    /**
     * Reads the RigidBodyInformation from a car and stores it in a map with Key = CarActorId, Value = List of Ballinformation for that car
     */
    private void parseRigidBodyInformation() throws FileServiceException {
        LOG.trace("Called - parseRigidBodyInformation");

        rigidBodyMap.putIfAbsent(actorId, new ArrayList<>());
        try {
            rigidBodyMap.get(actorId).add(rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused));
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about player found");
        }
    }



    LinkedHashMap<Integer, Integer> getPlayerCarMap() {
        return playerCarMap;
    }


    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }
}
