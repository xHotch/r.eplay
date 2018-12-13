package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.BoostInformation.BoostInformation;
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

    private RigidBodyParser rigidBodyParser;
    private ReadContext ctx;

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    public BoostInformationParser(RigidBodyParser rigidBodyParser) {this.rigidBodyParser = rigidBodyParser;}

    //Map that maps ActorID from a Player to a car. Key = boostActorId, Value = CarActorId
    private Map<Integer, Integer> carBoostMap = new HashMap<>();

    //Map that maps ActorID from a Car to a list of BoostAmountInformation. Key = boostActorId, Value = Boost amount
    private Map<Integer, List<BoostInformation>> boostAmountMap = new HashMap<>();

    private int currentBoost;

    void parse(int actorId, int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {


        this.actorId = actorId;
        this.currentFrame = currentFrame;
        this.currentActorUpdateNr = currentActorUpdateNr;
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.gamePaused = gamePaused;

        getPlayerIDfromBoost();
        //parse the amount of boost, check if the classname is not 'TAGame.CarComponent_TA:ReplicatedActive' since this indicates active boosting, will be done later.
        if(ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_TA:ReplicatedActive']") == null) {
            parseBoostAmountInformation();
        }

        LOG.debug("PRINT OUT BOOST INFORMATION");
        LOG.debug("Actor: " + actorId + ", Current Frame: " + currentFrame + ", current Boost: " + currentBoost);
        LOG.debug("PRINT OUT BOOST INFORMATION END");
    }

    private void getPlayerIDfromBoost() {
        LOG.trace("Called - getCarIDfromBoost");
        try {
            carBoostMap.putIfAbsent(actorId, ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_TA:Vehicle'].ActorId", Integer.class));
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost found");
        } catch (NullPointerException e) {
            LOG.debug("No Information about boost found - wrong replicated info found");
        }

        //try catch clauses sequentially, otherwise the first calls would break the others if the others were actually valid.
        try {
            //get the current boost amount of the actor
            currentBoost = ctx.read("$.Frames[" + currentFrame + "].ActorUpdates[" + currentActorUpdateNr + "].['TAGame.CarComponent_Boost_TA:ReplicatedBoostAmount']", Integer.class);
            //map to 0 - 100, in the json it is from 0 - 255
            currentBoost = (int)(currentBoost / 255.0 * 100.0);
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost amount found");
        } catch (NullPointerException e) {
            LOG.debug("No information about boost found - wrong replicated info found");
        }
    }

    private void parseBoostAmountInformation() throws FileServiceException {
        LOG.trace("Called - parseBoostAmountInformation");

        //create new boost object
        BoostInformation boost = new BoostInformation(frameTime, frameDelta, currentFrame, gamePaused, currentBoost);

        boostAmountMap.putIfAbsent(actorId, new ArrayList<>());
        try {
            boostAmountMap.get(actorId).add(boost);
        } catch (PathNotFoundException e) {
            LOG.debug("No Information about boost amount found");
        }
    }

    public void printDebugInformation() {

        LOG.debug("\n");
        for (Map.Entry<Integer, List<BoostInformation>> boost:boostAmountMap.entrySet()
        ) {
            for (BoostInformation info:boost.getValue()
                 ) {
                LOG.debug("Boost ID: " + boost.getKey() + ", Frame: " + info.getFrame() + ", Boost amount: " + info.getBoostAmount());
            }
        }
    }
}
