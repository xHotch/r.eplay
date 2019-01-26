package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.FrameDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class CarInformationParser {
    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    //Map that maps ActorID from a car to a player. Key = CarActorId, Value = playerActorId
    private Map<Integer, Integer> playerCarMap = new LinkedHashMap<>();

    private MultiValueMap<Integer, Pair<Integer, Double>> playerToCarAndFrameTimeMultiMap = new LinkedMultiValueMap<>();

    private Map<Integer, List<Pair<Integer, Double>>> carToPlayerAndFrameMap = new HashMap<>();
    //Map that maps ActorID from a Player to a list of RigidBodyInformation
    private Map<Integer, List<RigidBodyInformation>> playerToRigidBodyMap = new LinkedHashMap<>();

    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;

    public CarInformationParser(RigidBodyParser rigidBodyParser) {
        this.rigidBodyParser = rigidBodyParser;
    }

    /**
     * Calls other methods, to read information about cars from the json file.
     *
     * @param actorId              ActorID of the car
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param frameDelta           deltaTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     */
    void parse(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) {
        LOG.trace("Called - parse");

        parseRigidBodyInformation(getPlayerIDfromCar(currentFrame, currentActorUpdateNr, actorId, frameTime),currentFrame,currentActorUpdateNr,frameTime,gamePaused);

    }

    /**
     * Calls other methods, to read information about cars from the json file.
     *
     * @param actorId              the ActorID from the Car
     * @param frameDTO             the FrameDTO in which the parsed information gets stored
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     */
    void parseVideoFrame(int actorId, int currentFrame, int currentActorUpdateNr, FrameDTO frameDTO, boolean gamePaused, double frameTime) {
        LOG.trace("Called - parse");

        mapPlayerIDtoCarAndTime(actorId, currentFrame, currentActorUpdateNr, frameTime);
        parseFrameRigidBodyInformation(actorId,currentFrame,currentActorUpdateNr,frameDTO,gamePaused,frameTime);
    }

    /**
     * Setups up the Lists and Maps.
     * Has to be called every time a new file gets parsed
     */
    void setup(){
        playerCarMap = new LinkedHashMap<>();
        playerToCarAndFrameTimeMultiMap = new LinkedMultiValueMap<>();
        carToPlayerAndFrameMap = new HashMap<>();
        playerToRigidBodyMap = new LinkedHashMap<>();
    }

    /**
     * Reads the PlayerID of a player, if it is assigned to a car and puts it in the playerCarMap with Key = CarActorId, Value = playerActorId
     */
    private int getPlayerIDfromCar(int currentFrame, int currentActorUpdateNr, int actorId, double frameTime) {
        LOG.trace("Called - getPlayerIDfromCar");
        try {

            int playerID = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['Engine.Pawn:PlayerReplicationInfo'].ActorId");

            if (playerCarMap.containsKey(actorId)){
                LOG.debug("New Player {} for car {} (old PlayerActor {}), at frame {}", playerID, actorId, playerCarMap.get(actorId), currentFrame);
            }
            playerCarMap.putIfAbsent(actorId, playerID);

            carToPlayerAndFrameMap.putIfAbsent(actorId,new ArrayList<>());
            carToPlayerAndFrameMap.get(actorId).add(Pair.create(playerID,frameTime));

            return playerID;
        } catch (PathNotFoundException e) {
            //No Information about new CarActor found
        }


        return getPlayerIDfromCarAndTime(actorId,frameTime);

    }

    /**
     * Returns the actorID from the player for a specific car and time
     * @param actorID ActorID from the Car
     * @param frameTime the frameTime
     */
    int getPlayerIDfromCarAndTime(int actorID, double frameTime){
        List<Pair<Integer, Double>> playerAndTimeList = carToPlayerAndFrameMap.get(actorID);

        double compareTime = 0.0;
        int playerid = -1;
        for (Pair<Integer, Double> playerAndTime : playerAndTimeList) {
            if (playerAndTime.getValue() < frameTime && playerAndTime.getValue() > compareTime) {
                playerid = playerAndTime.getKey();
                compareTime = playerAndTime.getValue();
            }
        }
        return playerid;
    }

    /**
     * Reads the PlayerID of a player, if it is assigned to a car and puts it in the playerCarAndFrameTimeMap with Key = PlayerActorID, Value = carActorId and Frametime
     */
    private void mapPlayerIDtoCarAndTime(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime){
        LOG.trace("Called - mapPlayerIDtoCarAndTime");
        try {
            int playerID = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['Engine.Pawn:PlayerReplicationInfo'].ActorId");

            playerCarMap.putIfAbsent(actorId, playerID);
            playerToCarAndFrameTimeMultiMap.add(playerID, Pair.create(actorId,frameTime));

            carToPlayerAndFrameMap.putIfAbsent(actorId,new ArrayList<>());
            carToPlayerAndFrameMap.get(actorId).add(Pair.create(playerID,frameTime));

        } catch (PathNotFoundException e) {
            //No Information about new CarActor found
        }
    }

    /**
     * Reads the RigidBodyInformation from a car and stores it in a map with Key = CarActorId, Value = List of Ballinformation for that car
     */
    private void parseRigidBodyInformation(int playerID, int currentFrame, int currentActorUpdateNr, double frameTime, boolean gamePaused) {
        LOG.trace("Called - parseRigidBodyInformation");

        playerToRigidBodyMap.putIfAbsent(playerID, new ArrayList<>());
        try {
            playerToRigidBodyMap.get(playerID).add(rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime, gamePaused));

        } catch (PathNotFoundException e) {
            LOG.debug("No Information about player found");
        }
    }

    /**
     * Reads the RigidBodyInformation from a car and stores it in a map with Key = CarActorId, Value = List of Ballinformation for that car
     */
    private void parseFrameRigidBodyInformation(int actorId, int currentFrame, int currentActorUpdateNr, FrameDTO frameDTO, boolean gamePaused, double frameTime)  {
        LOG.trace("Called - parseRigidBodyInformation");

        try {
            frameDTO.getCarRigidBodyInformations().put(actorId,rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime, gamePaused));
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about player found");
        } 
    }

    Map<Integer, List<RigidBodyInformation>> getRigidBodyListPlayer() {
        return playerToRigidBodyMap;
    }

    Map<Integer, Integer> getPlayerCarMap() {
        return playerCarMap;
    }

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    MultiValueMap<Integer, Pair<Integer, Double>> getPlayerToCarAndFrameTimeMultiMap() {
        return playerToCarAndFrameTimeMultiMap;
    }

    public Map<Integer, List<Pair<Integer, Double>>> getCarToPlayerAndFrameMap() {
        return carToPlayerAndFrameMap;
    }
}
