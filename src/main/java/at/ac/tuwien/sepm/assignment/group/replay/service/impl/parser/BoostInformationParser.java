package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostIDs;
import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostDTO;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class BoostInformationParser {
    private int actorId;
    private int currentFrame;
    private int currentActorUpdateNr;
    private double frameTime;
    private double frameDelta;
    private boolean gamePaused;
    private int actorUpdateCount;

    private ReadContext ctx;
    private CarInformationParser carInformationParser;

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    public BoostInformationParser(CarInformationParser carInformationParser) {
        this.carInformationParser = carInformationParser;
    }

    //Retrieved from CarInformationParser
    private Map<Integer, Integer> carPlayerMap;

    private Map<Integer, Integer> carComponentToCarId;

    //Map that maps ActorID from a Car to a list of BoostAmountInformation. Key = playerId, Value = Boost amount information
    private Map<Integer, List<BoostDTO>> boostAmountMap;
    //Map that maps ActorID from a Car to a list of BoostPadDTO. Key = playerId, Value = Boost pad information
    private Map<Integer, Map<Integer, List<BoostPadDTO>>> boostPadMap;

    void setup(){
        carComponentToCarId = new HashMap<>();
        boostAmountMap = new HashMap<>();
        boostPadMap = new HashMap<>();
        carPlayerMap = carInformationParser.getPlayerCarMap();
    }

    /**
     * Calls other methods, to read information about boost amount from the json file.
     *
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param frameDelta           deltaTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     * @throws FileServiceException if the file couldn't be parsed
     */
    void parse(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {


        this.actorId = actorId;
        this.currentFrame = currentFrame;
        this.currentActorUpdateNr = currentActorUpdateNr;
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.gamePaused = gamePaused;

        getCarIDFromCarComponent();
        //parse the amount of boost, check if the classname is not 'TAGame.CarComponent_TA:ReplicatedActive' since this indicates active boosting, will be done later.
        parseBoostAmountInformation();

    }

    /**
     * Calls other methods, to read information about boost pads from the json file.
     *
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param frameDelta           deltaTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     * @throws FileServiceException if the file couldn't be parsed
     */
    void parseBoostPad(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused, int actorUpdateCount) throws FileServiceException {
        this.actorId = actorId;
        this.currentFrame = currentFrame;
        this.currentActorUpdateNr = currentActorUpdateNr;
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.gamePaused = gamePaused;
        this.actorUpdateCount = actorUpdateCount;

        //parse the boost pads information, if big or little pad was picked up and where.
        parseBoostPadInformation();
    }

    /**
     * Retrieves the car ID from the boost component and saves it in a map.
     */
    private void getCarIDFromCarComponent() {
        LOG.trace("Called - getCarIDfromBoost");
        try {
            int carComponentId = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].Id", Integer.class);
            int carActorId = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_TA:Vehicle'].ActorId", Integer.class);
            carComponentToCarId.putIfAbsent(carComponentId, carActorId);
        } catch (PathNotFoundException e) {
            // ignore
        }
    }

    /**
     * Parses the amount of boost for a player to a specific frame
     *
     */
    private void parseBoostAmountInformation() {
        LOG.trace("Called - parseBoostAmountInformation");

        try {
            if(ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_Boost_TA:ReplicatedBoostAmount']", Integer.class) != null) {
                int currentBoost = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_Boost_TA:ReplicatedBoostAmount']", Integer.class);
                //map to 0 - 100, in the json it is from 0 - 255
                currentBoost = (int) (currentBoost / 255.0 * 100.0);
                //create new boost object
                BoostDTO boost = new BoostDTO(frameTime, frameDelta, currentFrame, gamePaused, currentBoost);
                if(carComponentToCarId.containsKey(actorId)) {
                    int actualCarID = carComponentToCarId.get(actorId);
                    boostAmountMap.putIfAbsent(actualCarID, new ArrayList<>());
                    boostAmountMap.get(actualCarID).add(boost);
                }
            }
        } catch (PathNotFoundException e) {
            // ignore
        }

    }

    /**
     * Parses the boost pad informations
     *
     */
    private void parseBoostPadInformation() {
        LOG.trace("Called - parseBoostPadInformation");

        //create new boost pad object, little boost pad = 12, big boost pad = 100 boost.
        try {
            BoostPadDTO boostPad = new BoostPadDTO(frameTime, frameDelta, currentFrame, gamePaused);

            if(ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.VehiclePickup_TA:ReplicatedPickupData'].ActorId", Integer.class) != null) {
                int carID = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.VehiclePickup_TA:ReplicatedPickupData'].ActorId", Integer.class);

                int id = getPickedUpPosition(carID);

                // id == -1 --> no position found in the surrounding frames
                if(id != -1) {
                    boostPadMap.putIfAbsent(carID, new HashMap<>());
                    fillBoostPadIds(carID);
                    boostPadMap.get(carID).get(id).add(boostPad);
                }
            }
        } catch (PathNotFoundException e) {
            // ignore
        }
    }

    private int getPickedUpPosition(int carID) {
        //loop through all current updates and search for a position for the specific player to evaulate which boost pad was picked up
        String actorUpdatesLength = "].ActorUpdates.length()";
        int id = -1;
        try {
            // search position in the current frame
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, 0, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if(id != -1) {
                return id;
            }

            // search position in the n-1th frame
            int actorUpdateCount2 = ctx.read("$.Frames[" + (currentFrame - 1) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, -1, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if(id != -1) {
                return id;
            }

            // search position in the n+1th frame
            actorUpdateCount2 = ctx.read("$.Frames[" + (currentFrame + 1) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, 1, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if(id != -1) {
                return id;
            }

            // search position in the n-2th frame
            actorUpdateCount2 = ctx.read("$.Frames[" + (currentFrame - 2) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, -2, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if(id != -1) {
                return id;
            }

            // search position in the n+2th frame
            actorUpdateCount2 = ctx.read("$.Frames[" + (currentFrame + 2) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, 2, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if(id != -1) {
                return id;
            }
        } catch (PathNotFoundException e) {
            // ignore
        }
         return -1;
    }

    private int searchSurroundingFrames(int currentFrame, int frameOffset, int currentActorUpdate, int carID) {
        try {
            //if (ctx.read("$.Frames[" + (currentFrame + frameOffset) + "].ActorUpdates[" + currentActorUpdate + "].Id", Integer.class) != null) {
            int actID = ctx.read("$.Frames[" + (currentFrame + frameOffset) + "].ActorUpdates[" + currentActorUpdate + "].Id", Integer.class);

            if (actID == carID) {
                float x = ctx.read("$.Frames[" + (currentFrame + frameOffset) + "].ActorUpdates[" + currentActorUpdate + "].['TAGame.RBActor_TA:ReplicatedRBState'].Position.X", Float.class);
                float y = ctx.read("$.Frames[" + (currentFrame + frameOffset) + "].ActorUpdates[" + currentActorUpdate + "].['TAGame.RBActor_TA:ReplicatedRBState'].Position.Y", Float.class);
                float z = ctx.read("$.Frames[" + (currentFrame + frameOffset) + "].ActorUpdates[" + currentActorUpdate + "].['TAGame.RBActor_TA:ReplicatedRBState'].Position.Z", Float.class);
                return getIDFromPosition(x, y, z);
            }
            //}
        } catch (PathNotFoundException e) {
            // ignore
        }
        return -1;
    }

    private int getIDFromPosition(float x, float y, float z) {
        return BoostIDs.getID(x, y, z);
    }

    private void fillBoostPadIds(int actId) {
        for(int i=0; i<=33; i++) {
            boostPadMap.get(actId).putIfAbsent(i, new LinkedList<>());
        }
    }

    public Map<Integer, List<BoostDTO>> getBoostAmountMap() {
        LOG.trace("Called - boost amount calculate");
        Map<Integer, List<BoostDTO>> boostAmounts = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : carInformationParser.getPlayerCarMap().entrySet()) { // getValue() = playerKey, getKey() = carKey
            if (boostAmounts.containsKey(entry.getValue())) {
                if(boostAmountMap.containsKey(entry.getKey())) {
                    boostAmounts.get(entry.getValue()).addAll(boostAmountMap.get(entry.getKey()));
                }
            } else {
                if(boostAmountMap.containsKey(entry.getKey())) {
                    boostAmounts.put(entry.getValue(), boostAmountMap.get(entry.getKey()));
                }
            }
        }
        return boostAmounts;
    }

    public Map<Integer, Map<Integer, List<BoostPadDTO>>> getBoostPadMap() {
        // swap carID as keys and playerID as keys.
        LOG.trace("Called - boost pads calculate");
        Map<Integer, Map<Integer, List<BoostPadDTO>>> padMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : carInformationParser.getPlayerCarMap().entrySet()) { // getValue() = playerKey, getKey() = carKey
            if(boostPadMap.containsKey(entry.getKey())) {
                padMap.putIfAbsent(entry.getValue(), new HashMap<>());

                for(int i=0; i<=33; i++) {
                    padMap.get(entry.getValue()).putIfAbsent(i, new LinkedList<>());
                    padMap.get(entry.getValue()).get(i).addAll(boostPadMap.get(entry.getKey()).get(i));
                }
            }
        }
        return padMap;
    }
}
