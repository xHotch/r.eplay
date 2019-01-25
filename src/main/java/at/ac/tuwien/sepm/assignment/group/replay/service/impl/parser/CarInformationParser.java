package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.FrameDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
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
    private LinkedHashMap<Integer, Integer> playerCarMap = new LinkedHashMap<>();


    private MultiValueMap<Integer, Pair<Integer, Double>> playerToCarAndFrameTimeMap = new LinkedMultiValueMap<>();



    private Map<Integer, List<Pair<Integer, Double>>> carToPlayerAndFrameListMap = new HashMap<>();

    //Map that maps ActorID from a Player to a list of RigidBodyInformation
    private LinkedHashMap<Integer, List<RigidBodyInformation>> playerToRigidBodyMap = new LinkedHashMap<>();


    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;


    public CarInformationParser(RigidBodyParser rigidBodyParser) {
        this.rigidBodyParser = rigidBodyParser;
    }

    /**
     * Calls other methods, to read information about cars from the json file.
     *
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param frameDelta           deltaTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     * @throws FileServiceException if the file couldn't be parsed
     */
    void parse(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {
        LOG.trace("Called - parse");

        parseRigidBodyInformation(getPlayerIDfromCar(currentFrame, currentActorUpdateNr, actorId, frameTime),currentFrame,currentActorUpdateNr,frameTime,gamePaused);

    }


    void parseVideoFrame(int actorId, int currentFrame, int currentActorUpdateNr, FrameDTO frameDTO, boolean gamePaused, double frameTime) throws FileServiceException {
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
        playerToCarAndFrameTimeMap = new LinkedMultiValueMap<>();
        carToPlayerAndFrameListMap = new HashMap<>();
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

            carToPlayerAndFrameListMap.putIfAbsent(actorId,new ArrayList<>());
            carToPlayerAndFrameListMap.get(actorId).add(Pair.create(playerID,frameTime));

            return playerID;
        } catch (PathNotFoundException e) {
            //No Information about new CarActor found
        }

        List<Pair<Integer, Double>> playerAndTimeList = carToPlayerAndFrameListMap.get(actorId);

        double compareTime = 0.0;
        int playerid = -1;
        for (Pair<Integer, Double> playerAndTime : playerAndTimeList){
            if (playerAndTime.getValue() < frameTime){
                if(playerAndTime.getValue()>compareTime){
                    playerid = playerAndTime.getKey();
                    compareTime=playerAndTime.getValue();
                }
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
            playerToCarAndFrameTimeMap.add(playerID, Pair.create(actorId,frameTime));

            carToPlayerAndFrameListMap.putIfAbsent(actorId,new ArrayList<>());
            carToPlayerAndFrameListMap.get(actorId).add(Pair.create(playerID,frameTime));

        } catch (PathNotFoundException e) {
            //No Information about new CarActor found
        }
    }


    /**
     * Reads the RigidBodyInformation from a car and stores it in a map with Key = CarActorId, Value = List of Ballinformation for that car
     */
    private void parseRigidBodyInformation(int playerID, int currentFrame, int currentActorUpdateNr, double frameTime, boolean gamePaused) throws FileServiceException {
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
    private void parseFrameRigidBodyInformation(int actorId, int currentFrame, int currentActorUpdateNr, FrameDTO frameDTO, boolean gamePaused, double frameTime) throws FileServiceException {
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

    LinkedHashMap<Integer, Integer> getPlayerCarMap() {
        return playerCarMap;
    }


    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }


    MultiValueMap<Integer, Pair<Integer, Double>> getPlayerToCarAndFrameTimeMap() {
        return playerToCarAndFrameTimeMap;
    }

    public Map<Integer, List<Pair<Integer, Double>>> getCarToPlayerAndFrameListMap() {
        return carToPlayerAndFrameListMap;
    }
}
