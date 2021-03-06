package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostIDs;
import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.*;

@Service
public class BoostInformationParser {
    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ReadContext ctx;
    private CarInformationParser carInformationParser;

    private static final String FRAMESTRING = "$.Frames[";
    private static final String ACTORUPDATESTRING = "].ActorUpdates[";

    //Map that maps car component to car id. Key = componentId, Value = carActorId
    private Map<Integer, Integer> carComponentToCarId;

    //Map that maps ActorID from a Component to a list of BoostAmountInformation. Key = playerId, Value = Boost amount information
    private Map<Integer, List<BoostDTO>> componentBoostAmountMap;
    //Map that maps ActorID from a Car to a list of BoostPadDTO. Key = playerId, Value = Boost pad information
    private Map<Integer, Map<Integer, List<BoostPadDTO>>> boostPadMap;

    public BoostInformationParser(CarInformationParser carInformationParser) {
        this.carInformationParser = carInformationParser;
    }
    /**
     * Setup the maps each time a replay gets uploaded
     */
    void setup() {
        carComponentToCarId = new HashMap<>();
        boostPadMap = new HashMap<>();
        componentBoostAmountMap = new HashMap<>();
    }

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Calls other methods, to read information about boost amount from the json file.
     *
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param frameDelta           deltaTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     */
    void parse(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) {

        getCarIDFromCarComponent(currentFrame, currentActorUpdateNr);
        //parse the amount of boost, check if the classname is not 'TAGame.CarComponent_TA:ReplicatedActive' since this indicates active boosting, will be done later.
        parseBoostAmountInformation(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
    }

    /**
     * Calls other methods, to read information about boost pads from the json file.
     *
     * @param currentFrame         ID of the frame to parse
     * @param currentActorUpdateNr ID of the ActorUpdate to parse
     * @param frameTime            frameTime of the frame
     * @param frameDelta           deltaTime of the frame
     * @param gamePaused           boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     */
    void parseBoostPad(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused, int actorUpdateCount) {
        //parse the boost pads information, if big or little pad was picked up and where.
        parseBoostPadInformation(currentFrame, currentActorUpdateNr, actorUpdateCount, frameTime, frameDelta, gamePaused);
    }

    /**
     * Retrieves the car ID from the boost component and saves it in a map.
     */
    private void getCarIDFromCarComponent(int currentFrame, int currentActorUpdateNr) {
        LOG.trace("Called - getCarIDfromBoost");
        try {
            int carComponentId = ctx.read(FRAMESTRING + currentFrame + ACTORUPDATESTRING + currentActorUpdateNr + "].Id", Integer.class);
            int carActorId = ctx.read(FRAMESTRING + currentFrame + ACTORUPDATESTRING + currentActorUpdateNr + "].['TAGame.CarComponent_TA:Vehicle'].ActorId", Integer.class);
            carComponentToCarId.putIfAbsent(carComponentId, carActorId);
        } catch (PathNotFoundException e) {
            // ignore
        }
    }

    /**
     * Parses the amount of boost for a player to a specific frame
     */
    private void parseBoostAmountInformation(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) {
        LOG.trace("Called - parseBoostAmountInformation");

        try {
            if (ctx.read(FRAMESTRING + currentFrame + ACTORUPDATESTRING + currentActorUpdateNr + "].['TAGame.CarComponent_Boost_TA:ReplicatedBoostAmount']", Integer.class) != null) {
                int currentBoost = ctx.read(FRAMESTRING + currentFrame + ACTORUPDATESTRING + currentActorUpdateNr + "].['TAGame.CarComponent_Boost_TA:ReplicatedBoostAmount']", Integer.class);
                //map to 0 - 100, in the json it is from 0 - 255
                currentBoost = (int) (currentBoost / 255.0 * 100.0);
                //create new boost object
                BoostDTO boost = new BoostDTO(frameTime, frameDelta, currentFrame, gamePaused, currentBoost);
                componentBoostAmountMap.putIfAbsent(actorId, new ArrayList<>());
                componentBoostAmountMap.get(actorId).add(boost);

            }
        } catch (PathNotFoundException e) {
            // ignore
        }

    }

    /**
     * Parses the boost pad informations
     */
    private void parseBoostPadInformation(int currentFrame, int currentActorUpdateNr, int actorUpdateCount, double frameTime, double frameDelta, boolean gamePaused) {
        LOG.trace("Called - parseBoostPadInformation");

        //create new boost pad object, little boost pad = 12, big boost pad = 100 boost.
        try {
            BoostPadDTO boostPad = new BoostPadDTO(frameTime, frameDelta, currentFrame, gamePaused);

            int carID = ctx.read(FRAMESTRING + currentFrame + ACTORUPDATESTRING + currentActorUpdateNr + "].['TAGame.VehiclePickup_TA:ReplicatedPickupData'].ActorId", Integer.class);

            int id = getPickedUpPosition(carID, currentFrame, actorUpdateCount);

            // id == -1 --> no position found in the surrounding frames
            if (id != -1) {

                int playerId = carInformationParser.getPlayerIDfromCarAndTime(carID, frameTime);

                boostPadMap.putIfAbsent(playerId, new HashMap<>());
                fillBoostPadIds(playerId);
                boostPadMap.get(playerId).get(id).add(boostPad);
            }
        } catch (PathNotFoundException e) {
            // ignore
        }
    }

    /**
     * Evaluates the position where the boost pad was picked up
     *
     * @param carID            the car that picked up the boost pad
     * @param currentFrame     the current frame where the event has happened
     * @param actorUpdateCount number of actor updates for the current frame
     * @return the id of the boostpad
     */
    private int getPickedUpPosition(int carID, int currentFrame, int actorUpdateCount) {
        //loop through all current updates and search for a position for the specific player to evaulate which boost pad was picked up
        String actorUpdatesLength = "].ActorUpdates.length()";
        int id = -1;
        try {
            // search position in the current frame
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount && id == -1; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, 0, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if (id != -1) {
                return id;
            }

            // search position in the n-1th frame
            int actorUpdateCount2 = ctx.read(FRAMESTRING + (currentFrame - 1) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2 && id == -1; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, -1, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if (id != -1) {
                return id;
            }

            // search position in the n+1th frame
            actorUpdateCount2 = ctx.read(FRAMESTRING + (currentFrame + 1) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2 && id == -1; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, 1, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if (id != -1) {
                return id;
            }

            // search position in the n-2th frame
            actorUpdateCount2 = ctx.read(FRAMESTRING + (currentFrame - 2) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2 && id == -1; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, -2, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if (id != -1) {
                return id;
            }

            // search position in the n+2th frame
            actorUpdateCount2 = ctx.read(FRAMESTRING + (currentFrame + 2) + actorUpdatesLength);
            for (int currentActorUpdate = 0; currentActorUpdate < actorUpdateCount2 && id == -1; currentActorUpdate++) {
                id = searchSurroundingFrames(currentFrame, 2, currentActorUpdate, carID);
            }
            // return the id if a position was found in the surrounding frames
            if (id != -1) {
                return id;
            }
        } catch (PathNotFoundException e) {
            // ignore
        }
        return -1;
    }

    /**
     * Searches for a position of the player that picked up the boost in the given frame
     *
     * @param currentFrame       current frame
     * @param frameOffset        frame offset
     * @param currentActorUpdate current actor update
     * @param carID              car ID to search for
     * @return the id of the boost pad
     */
    private int searchSurroundingFrames(int currentFrame, int frameOffset, int currentActorUpdate, int carID) {
        try {
            int actID = ctx.read(FRAMESTRING + (currentFrame + frameOffset) + ACTORUPDATESTRING + currentActorUpdate + "].Id", Integer.class);

            if (actID == carID) {
                float x = ctx.read(FRAMESTRING + (currentFrame + frameOffset) + ACTORUPDATESTRING + currentActorUpdate + "].['TAGame.RBActor_TA:ReplicatedRBState'].Position.X", Float.class);
                float y = ctx.read(FRAMESTRING + (currentFrame + frameOffset) + ACTORUPDATESTRING + currentActorUpdate + "].['TAGame.RBActor_TA:ReplicatedRBState'].Position.Y", Float.class);
                float z = ctx.read(FRAMESTRING + (currentFrame + frameOffset) + ACTORUPDATESTRING + currentActorUpdate + "].['TAGame.RBActor_TA:ReplicatedRBState'].Position.Z", Float.class);
                return getIDFromPosition(x, y, z);
            }
        } catch (PathNotFoundException e) {
            // ignore
        }
        return -1;
    }

    /**
     * Retrieves the id from the position
     *
     * @param x x value
     * @param y y value
     * @param z z value
     * @return the id of the boost pad
     */
    private int getIDFromPosition(float x, float y, float z) {
        return BoostIDs.getID(x, y, z);
    }

    private void fillBoostPadIds(int actId) {
        for (int i = 0; i <= 33; i++) {
            boostPadMap.get(actId).putIfAbsent(i, new LinkedList<>());
        }
    }

    /**
     * Gets the boost amount map
     *
     * @return boost amount map
     */
    public Map<Integer, List<BoostDTO>> getBoostAmountMap() {
        LOG.trace("Called - getBoostAmountMap");

        Map<Integer, List<BoostDTO>> carBoostAmounts = new HashMap<>();
        for (Map.Entry<Integer, List<BoostDTO>> entry : componentBoostAmountMap.entrySet()) {
            if (carComponentToCarId.containsKey(entry.getKey())) {
                int carID = carComponentToCarId.get(entry.getKey());
                carBoostAmounts.putIfAbsent(carID, new ArrayList<>());
                carBoostAmounts.get(carID).addAll(entry.getValue());
            } else {
                LOG.debug("No car found for component {} with {} entries", entry.getKey(), entry.getValue().size());
            }
        }

        Map<Integer, List<BoostDTO>> boostAmounts = new HashMap<>();

        for (Map.Entry<Integer, List<Pair<Integer, Double>>> entry : carInformationParser.getCarToPlayerAndFrameMap().entrySet()) { // getValue() = playerKey, getKey() = carKey
            if (carBoostAmounts.containsKey(entry.getKey())) {
                List<BoostDTO> boost = carBoostAmounts.get(entry.getKey());
                for (BoostDTO boostDTO : boost) {
                    double frameTime = boostDTO.getFrameTime();
                    double playerTime = 0.0;
                    for (Pair<Integer, Double> pair : entry.getValue()) {
                        if (frameTime > pair.getValue() && (pair.getValue() > playerTime)) {
                            playerTime = pair.getValue();
                            boostAmounts.putIfAbsent(pair.getKey(), new ArrayList<>());
                            boostAmounts.get(pair.getKey()).add(boostDTO);
                        }
                    }
                }
            }
        }
        return boostAmounts;
    }

    /**
     * Gets the boost pad map
     *
     * @return boost pad map
     */
    Map<Integer, Map<Integer, List<BoostPadDTO>>> getBoostPadMap() {

        LOG.trace("Called - getBoostPadMap");


        return boostPadMap;
    }
}
