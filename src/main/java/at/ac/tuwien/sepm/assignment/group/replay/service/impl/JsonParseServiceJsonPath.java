package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service Class that parses .json files using JsonPath
 *
 * @author Markus Kogelbauer
 */
@Service
public class JsonParseServiceJsonPath implements JsonParseService {

    //Logger
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private File jFile;
    private ReadContext ctx;

    //Strings
    private String rigidBody = "['TAGame.RBActor_TA:ReplicatedRBState']";
    private String rigidBodyPosition = rigidBody + ".Position";
    private String rigidBodyRotation = rigidBody + ".Rotation";
    private String rigidBodyLinearVelocity = rigidBody + ".LinearVelocity";
    private String rigidBodyAngularVelocity = rigidBody + ".AngularVelocity";


    @Override
    public MatchDTO parseMatch(File jsonFile) throws FileServiceException {
        LOG.trace("called - parseMatch");
        if (jsonFile == null) {

            throw new FileServiceException("Can't parse null");
        }
        String extension = FilenameUtils.getExtension(jsonFile.getName());
        if (!extension.equals("json")) {
            throw new FileServiceException("wrong file type: " + extension);
        }
        if (!jsonFile.equals(jFile) || ctx == null) {
            try {
                Configuration conf = Configuration.defaultConfiguration();
                Configuration conf2 = conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
                ctx = JsonPath.using(conf2).parse(jsonFile);
                jFile = jsonFile;
            } catch (IOException e) {
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }

        }

        //Map that contains the ids and Classnames from actors.
        //Actors can referene each other, e.g. a car references a player by setting ['Engine.Pawn:PlayerReplicationInfo'] to the ActorID of the player
        HashMap<Integer, String> actors = new HashMap<>();

        //Create List that stores calculated ballInformations. is used to generate Ball statistics
        ArrayList<RigidBodyInformation> ballInformations = new ArrayList<>();

        try {

            int frameCount = ctx.read("$.Frames.length()");

            LOG.debug("Match framecount : {}", frameCount);
            for (int i = 0; i < frameCount; i++) {

                String frame = "$.Frames[" + i + "]";
                int actorUpdateCount = ctx.read(frame + ".ActorUpdates.length()");
                LOG.debug("Frame {} has {} ActorUpdates", i, actorUpdateCount);


                double frameTime = ctx.read(frame + ".Time", Double.class);
                double frameDelta = ctx.read(frame + ".Delta", Double.class);


                for (int j = 0; j < actorUpdateCount; j++) {

                    int actorId = ctx.read(frame + ".ActorUpdates[" + j + "].Id", Integer.class);

                    //New Actors have field ClassName. Check if it is a new actor
                    String newActor = ctx.read(frame + ".ActorUpdates[" + j + "].ClassName");
                    if (newActor != null) {
                        actors.put(actorId, newActor);
                        LOG.debug("New Actor found at frame {}, actorupdate {}", i, j);
                    }


                    String className = actors.get(actorId);


                    switch (className) {
                        case "TAGame.Ball_TA":
                            //todo parse information, if game is paused and replace 'false' on parameter gamePaused
                            ballInformations.add(parseRigidBodyInformation(i, j, frameTime, frameDelta, false));
                            break;
                        case "TAGame.Car_TA":
                            //parseCarInformation(i,j,frameTime,frameDelta);
                            break;
                        case "TAGame.PRI_TA":
                            //parsePlayerInformation(i,j);
                            break;
                        case "TAGamee.GRI_TA":
                            //parseMatchInformation
                            //e.g ['ProjectX.GRI_X:ReplicatetdGamePlaylist'] -> MatchType id

                        default:
                            //Information not relevant for our project
                            break;

                    }

                    LOG.debug(className);
                }
            }
        } catch (Exception e) {
            throw new FileServiceException("Exception while parsing frames", e);
        }

        //todo implement
        generateBallStatistic(ballInformations);


        //Todo parse Player information from Frames not Properties
        return readProperties();
    }


    /**
     * Reads Match propertiees and generates MatchDTO
     * Information not accurate, refactor to read playerInfo from frames
     *
     * @return MatchDTO created from the analysed Json file
     * @throws FileServiceException if the file couldn't be parsed
     */
    private MatchDTO readProperties() throws FileServiceException {
        MatchDTO match = new MatchDTO();
        try {
            String dateTime = ctx.read("$.Properties.Date");
            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
            match.setDateTime(LocalDateTime.parse(dateTime, dtFormatter));
            match.setTeamSize(ctx.read("$.Properties.TeamSize"));
            match.setReadId(ctx.read("$.Properties.Id"));
            List<MatchPlayerDTO> playerList = new ArrayList<>();
            for (int i = 0; i < match.getTeamSize() * 2; i++) {


                PlayerDTO playerDTO = new PlayerDTO();
                MatchPlayerDTO matchPlayer = new MatchPlayerDTO();

                matchPlayer.setMatchDTO(match);
                matchPlayer.setPlayerDTO(playerDTO);

                matchPlayer.setAssists(ctx.read("$.Properties.PlayerStats[" + i + "].Assists"));
                matchPlayer.setGoals(ctx.read("$.Properties.PlayerStats[" + i + "].Goals"));
                matchPlayer.setSaves(ctx.read("$.Properties.PlayerStats[" + i + "].Saves"));
                matchPlayer.setShots(ctx.read("$.Properties.PlayerStats[" + i + "].Shots"));
                matchPlayer.setScore(ctx.read("$.Properties.PlayerStats[" + i + "].Score"));
                playerDTO.setName(ctx.read("$.Properties.PlayerStats[" + i + "].Name"));
                matchPlayer.setTeam(ctx.read("$.Properties.PlayerStats[" + i + "].Team"));

                long id = ctx.read("$.Properties.PlayerStats[" + i + "].OnlineID");
                if (id == 0) {
                    throw new FileServiceException("A player has no id");
                }
                playerDTO.setPlatformID(id);
                playerList.add(matchPlayer);
            }
            match.setPlayerData(playerList);


        } catch (Exception e) {
            throw new FileServiceException(e.getMessage(), e);
        }
        return match;
    }


    /**
     * Reads Information about RigidBodys from specific frame / actorUpdate
     *
     * @param frameId       ID of the frame to parse
     * @param actorUpdateId ID of the ActorUpdate to parse
     * @param frameTime     frameTime of the frame
     * @param frameDelta    deltaTime of the frame
     * @param gamePaused    boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     * @return RigidBodyInformation Containing rotation, velocity and position information, as well as frame and delta time values.
     * @throws FileServiceException if the file couldn't be parsed
     */
    private RigidBodyInformation parseRigidBodyInformation(int frameId, int actorUpdateId, double frameTime, double frameDelta, boolean gamePaused) throws FileServiceException {
        LOG.trace("Called - parseRigidBodyInformation");

        //todo boolean sleeping

        //todo check if game is paused
        LinkedHashMap<String, Object> position = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyPosition);
        LinkedHashMap<String, Object> rotation = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyRotation);
        LinkedHashMap<String, Object> linearVelocity = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyLinearVelocity);
        LinkedHashMap<String, Object> angularVelocity = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyAngularVelocity);

        RigidBodyInformation rigidBodyInformation = new RigidBodyInformation();

        try {
            rigidBodyInformation.setPosition(getVectorFromMap(position));
            rigidBodyInformation.setAngularVelocity(getVectorFromMap(angularVelocity));
            rigidBodyInformation.setLinearVelocity(getVectorFromMap(linearVelocity));
            rigidBodyInformation.setRotation(getQuaternionFromMap(rotation));
        } catch (NullPointerException e) {
            //Catches NullPointerException as null is be returned by JsonPath if the value was not updated
        } catch (Exception e) {
            throw new FileServiceException("Error while calculating RigidBodyInformation", e);
        }

        rigidBodyInformation.setGamePaused(gamePaused);
        rigidBodyInformation.setFrameTime(frameTime);
        rigidBodyInformation.setFrameDelta(frameDelta);

        return rigidBodyInformation;
    }

    //todo Refactor in seperate Statistic Class
    private void generateBallStatistic(List<RigidBodyInformation> rigidBodyInformations) {
        //debug("Match has {} ballinformations", rigidBodyInformations.size());
    }

    /**
     * Generates a Vector3D from LinkedHashMap generated by JsonPath.
     * Vector3D are better for calculation, compared  to the map.
     * <p>
     * Velocities and Positions are stored as 3 dimensional Vectors
     *
     * @param map LinkedHashMap parsed by JsonPath, containing 3 dimensional Vector values
     * @return Vector3D containing the x,y,z values from the map
     */
    private Vector3D getVectorFromMap(LinkedHashMap<String, Object> map) {
        int i = 0;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                if (i == 0) {
                    x = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 1) {
                    y = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 2) {
                    z = ((Integer) entry.getValue()).doubleValue();
                }
            } else {
                if (i == 0) {
                    x = (double) entry.getValue();
                }
                if (i == 1) {
                    y = (double) entry.getValue();
                }
                if (i == 2) {
                    z = (double) entry.getValue();
                }

                i++;
            }
        }

        return new Vector3D(x, y, z);
    }

    /**
     * Generates a Quaternion from LinkedHashMap generated by JsonPath.
     * Quaternions are better for calculations, compared  to the map.
     * <p>
     * Rotations are stored as Quaternions
     *
     * @param map LinkedHashMap parsed by JsonPath, containing Quaternion values
     * @return Quaternion containing the x,x,y,z values from the map
     */
    private Quaternion getQuaternionFromMap(LinkedHashMap<String, Object> map) {
        int i = 0;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double w = 0.0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                if (i == 0) {
                    x = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 1) {
                    y = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 2) {
                    z = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 3) {
                    w = ((Integer) entry.getValue()).doubleValue();
                }
            } else {
                if (i == 0) {
                    x = (double) entry.getValue();
                }
                if (i == 1) {
                    y = (double) entry.getValue();
                }
                if (i == 2) {
                    z = (double) entry.getValue();
                }
                if (i == 3) {
                    w = (double) entry.getValue();
                }
            }
            i++;

        }

        //todo test quaternion order
        //return new Quaternion(x,y,z,w);
        return new Quaternion(w, x, y, z);
    }
}

