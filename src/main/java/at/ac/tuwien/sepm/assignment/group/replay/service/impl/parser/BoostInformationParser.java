package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.BoostInformation.BoostInformation;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.BoostInformation.BoostPadInformation;
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
public class BoostInformationParser {
    private int actorId;
    private int currentFrame;
    private int currentActorUpdateNr;
    private double frameTime;
    private double frameDelta;
    private boolean gamePaused;

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

    private Map<Integer, Integer> carComponentToCarId = new HashMap<>();
    //Map that maps ActorID from a Player to a car. Key = carId, Value = playerId
    private Map<Integer, Integer> carBoostMap = new HashMap<>();
    //Map that maps ActorID from a boost pad to a car/player. Key = carId, Value = playerId
    private Map<Integer, Integer> carBoostPadMap = new HashMap<>();

    //Map that maps ActorID from a Car to a list of BoostAmountInformation. Key = playerId, Value = Boost amount information
    private Map<Integer, List<BoostInformation>> boostAmountMap = new HashMap<>();
    //Map that maps ActorID from a Car to a list of BoostPadInformation. Key = playerId, Value = Boost pad information
    private Map<Integer, List<BoostPadInformation>> boostPadMap = new HashMap<>();

    void setup(){
        carComponentToCarId = new HashMap<>();
        carBoostMap = new HashMap<>();
        boostAmountMap = new HashMap<>();
        boostPadMap = new HashMap<>();
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
        getPlayerIDfromBoost();
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
    void parseBoostPad(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {
        this.actorId = actorId;
        this.currentFrame = currentFrame;
        this.currentActorUpdateNr = currentActorUpdateNr;
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.gamePaused = gamePaused;

        getPlayerIDfromBoostPad();
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
            LOG.debug("No Information about boost found");
        } catch (NullPointerException e) {
            LOG.debug("No Information about boost found - wrong replication info found");
        }
    }

    /**
     * Retrieves the player actor ID from the corresponding car actor ID and saves it in a map.
     */
    private void getPlayerIDfromBoost() {
        LOG.trace("Called - getCarIDfromBoost");
        try {
            int carActorId = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_TA:Vehicle'].ActorId", Integer.class);
            Map<Integer, Integer> carPlayerMap = carInformationParser.getPlayerCarMap();
            int playerId = carPlayerMap.get(carActorId);
            carBoostMap.putIfAbsent(carActorId, playerId);
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost found");
        } catch (NullPointerException e) {
            LOG.debug("No Information about boost found - wrong replicated info found");
        }
    }

    /**
     * Retrieves the player actor ID from the corresponding car boost component that picked up the boost.
     */
    private void getPlayerIDfromBoostPad() {
        LOG.trace("Called - getPlayerIDfromBoostPad");
        try {
            int carActorId = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.VehiclePickup_TA:ReplicatedPickupData'].ActorId", Integer.class);
            Map<Integer, Integer> carPlayerMap = carInformationParser.getPlayerCarMap();
            int playerId = carPlayerMap.get(carActorId);
            carBoostPadMap.putIfAbsent(carActorId, playerId);
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost pad found");
        } catch (NullPointerException e) {
            LOG.debug("No Information about boost pad found - wrong replicated info found");
        }
    }

    /**
     * Parses the amount of boost for a player to a specific frame
     *
     * @throws FileServiceException if the file couldn't be parsed
     */
    private void parseBoostAmountInformation() throws FileServiceException {
        LOG.trace("Called - parseBoostAmountInformation");

        try {
            //get the current boost amount of the actor
            int currentBoost = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_Boost_TA:ReplicatedBoostAmount']", Integer.class);
            //map to 0 - 100, in the json it is from 0 - 255
            currentBoost = (int)(currentBoost / 255.0 * 100.0);
            //create new boost object
            BoostInformation boost = new BoostInformation(frameTime, frameDelta, currentFrame, gamePaused, currentBoost);
            boostAmountMap.putIfAbsent(actorId, new ArrayList<>());
            boostAmountMap.get(actorId).add(boost);

        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost amount found");
        } catch (NullPointerException e) {
            LOG.debug("No information about boost found - wrong replicated info found");
        }

    }

    /**
     * Parses the boost pad informations
     *
     * @throws FileServiceException if the file couldn't be parsed
     */
    private void parseBoostPadInformation() throws FileServiceException {
        LOG.trace("Called - parseBoostPadInformation");

        //create new boost pad object, little boost pad = 12, big boost pad = 100 boost.
        try {
            //get the over map constant id from the boost pad from the type name.
            String typeName = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].TypeName");
            String substring = typeName.substring(typeName.length()-2, typeName.length());

            int id = -1;
            if(!substring.isEmpty() && substring.contains("_")) {
                id = Integer.parseInt(substring.substring(1));
            } else {
                id = Integer.parseInt(substring);
            }

            BoostPadInformation boostPad = new BoostPadInformation(frameTime, frameDelta, currentFrame, gamePaused, id);

            int actId = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.VehiclePickup_TA:ReplicatedPickupData'].ActorId", Integer.class);
            boostPadMap.putIfAbsent(actId, new ArrayList<>());
            boostPadMap.get(actId).add(boostPad);
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost amount found");
        } catch (NullPointerException e) {
            LOG.debug("No such path found");
        }
    }

    public void printDebugInformation() {

        LOG.debug("\n");
        for (Map.Entry<Integer, Integer> carComponents:carComponentToCarId.entrySet()
        ) {
            LOG.debug("Car Boost component ID: {}, Car ID {}", carComponents.getKey(), carComponents.getValue());
        }

        for (Map.Entry<Integer, Integer> carIDs:carBoostMap.entrySet()
        ) {
            LOG.debug("(Boost amount) Car ID: {}, Player ID {}", carIDs.getKey(), carIDs.getValue());
        }

        for (Map.Entry<Integer, Integer> carIDs:carBoostPadMap.entrySet()
        ) {
            LOG.debug("(Boost pads) Car ID: {}, Player ID {}", carIDs.getKey(), carIDs.getValue());
        }

        for (Map.Entry<Integer, List<BoostPadInformation>> boost:boostPadMap.entrySet()
        ) {
            for (BoostPadInformation info:boost.getValue()
            ) {
                LOG.debug("Car ID: {}, Frame time: {}, Boost pad ID: {}", boost.getKey(), info.getFrameTime(), info.getBoostPadId());
            }
        }
    }

    public Map<Integer, Integer> getCarBoostMap() {
        return carBoostMap;
    }

    public Map<Integer, Integer> getCarBoostPadMap() {
        // contains the carID to playerID mappings
        return carBoostPadMap;
    }

    public Map<Integer, List<BoostInformation>> getBoostAmountMap() {
        return boostAmountMap;
    }

    public Map<Integer, List<BoostPadInformation>> getBoostPadMap() {
        // contains the carID and boost pad ID that was picked up
        return boostPadMap;
    }
}
