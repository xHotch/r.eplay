package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
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
public class BallInformationParser {

    private ArrayList<RigidBodyInformation> rigidBodyInformations = new ArrayList<>();

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

    //Map that maps ActorID from a Player to a car. Key = CarActorId, Value = playerActorId
    private Map<Integer, Integer> playerCarMap = new HashMap<>();


    //Map that maps ActorID from a Car to a list of RigidBodyInformation
    private Map<Integer, List<RigidBodyInformation>> rigidBodyMap = new HashMap<>();

    void parse(int currentFrame, int currentActorUpdateNr, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {
        rigidBodyInformations.add(rigidBodyParser.parseRigidBodyInformation(currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused));
    }

}