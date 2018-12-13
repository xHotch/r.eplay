package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private RigidBodyParser rigidBodyParser;
    private PlayerInformationParser playerInformationParser;
    private GameInformationParser gameInformationParse;
    private CarInformationParser carInformationParser;
    private BallInformationParser ballInformationParser;
    private BoostInformationParser boostInformationParser;


    public JsonParseServiceJsonPath(RigidBodyParser rigidBodyParser, PlayerInformationParser playerInformationParser, GameInformationParser gameInformationParse, CarInformationParser carInformationParser, BallInformationParser ballInformationParser, BoostInformationParser boostInformationParser) {
        this.rigidBodyParser = rigidBodyParser;
        this.playerInformationParser = playerInformationParser;
        this.gameInformationParse = gameInformationParse;
        this.carInformationParser = carInformationParser;
        this.ballInformationParser = ballInformationParser;
        this.boostInformationParser = boostInformationParser;
    }

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
                Configuration conf = Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
                ctx = JsonPath.using(conf).parse(jsonFile);

                playerInformationParser.setCtx(ctx);
                rigidBodyParser.setCtx(ctx);
                gameInformationParse.setCtx(ctx);
                carInformationParser.setCtx(ctx);
                ballInformationParser.setCtx(ctx);
                boostInformationParser.setCtx(ctx);

                jFile = jsonFile;
            } catch (IOException e) {
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }

        }

        parseFrames();
        LOG.debug("asdf");
        //Todo parse Player information from Frames not Properties
        return readProperties();
    }


    private void parseFrames() throws FileServiceException {
        LOG.trace("Called - parseFrames");
        //Map that contains the ids and Classnames from actors.
        //Actors can referene each other, e.g. a car references a player by setting ['Engine.Pawn:PlayerReplicationInfo'] to the ActorID of the player
        HashMap<Integer, String> actors = new HashMap<>();

        //list that stores time when goals were scored, in order to pause the game afterwards
        ArrayList<Double> timeOfGoals = getTimeOfGoals();

        //pause game at the beginning
        boolean gamePaused = true;

        try {

            int frameCount = ctx.read("$.Frames.length()");

            LOG.debug("Match framecount : {}", frameCount);
            for (int currentFrame = 0; currentFrame < frameCount; currentFrame++) {

                String frame = "$.Frames[" + currentFrame + "]";
                int actorUpdateCount = ctx.read(frame + ".ActorUpdates.length()");
                LOG.debug("Frame {} has {} ActorUpdates", currentFrame, actorUpdateCount);


                double frameTime = ctx.read(frame + ".Time", Double.class);
                double frameDelta = ctx.read(frame + ".Delta", Double.class);

                //pause game if goal was scored
                if (!gamePaused){
                gamePaused = pauseGame(timeOfGoals, frameTime);}

                for (int currentActorUpdateNr = 0; currentActorUpdateNr < actorUpdateCount; currentActorUpdateNr++) {

                    int actorId = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].Id", Integer.class);

                    //New Actors have field ClassName. Check if it is a new actor
                    String newActor = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].ClassName");
                    if (newActor != null) {
                        actors.put(actorId, newActor);
                        LOG.debug("New Actor found at frame {}, actorupdate {}", currentFrame, currentActorUpdateNr);
                    }

                    String className = actors.get(actorId);

                    //resume game if countdown, that is shown after a goal before the game resumes, equals 0
                    Integer countdown = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].['TAGame.GameEvent_TA:ReplicatedRoundCountDownNumber']", Integer.class);

                    if ((countdown != null) && (countdown == 0)) {
                        gamePaused = false;
                    }

                    switch (className) {
                        case "TAGame.Ball_TA":
                            ballInformationParser.parse(currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;
                        case "TAGame.Car_TA":
                            carInformationParser.parse(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;

                        case "TAGame.PRI_TA":
                            //parsePlayerInformation(i,j);

                            break;
                        case "TAGamee.GRI_TA":
                            //parseMatchInformation
                            //e.g ['ProjectX.GRI_X:ReplicatetdGamePlaylist'] -> MatchType id
                            break;

                        case "TAGame.CarComponent_Boost_TA":
                            boostInformationParser.parse(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;

                        default:
                            //Information not relevant for our project
                            break;

                    }

                    LOG.debug(className);
                }
            }
            //boostInformationParser.printDebugInformation();
        } catch (Exception e) {
            throw new FileServiceException("Exception while parsing frames", e);
        }
    }


    /**
     * checks if the game should be paused by checking if a goal was scored
     *
     * @param timeOfGoals list that contains each moment of a scored goal
     * @param frameTime   current frame time
     * @return true if game should be paused
     * false if game should not be paused
     */
    private boolean pauseGame(ArrayList<Double> timeOfGoals, Double frameTime) {
        if (!timeOfGoals.isEmpty()) {
            for (Double goalTime : timeOfGoals) {
                if (Double.compare(goalTime, frameTime) == 0) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * reads the time of each goal out of the json file and returns it in a list
     *
     * @return a list of Double values that contains each moment a goal was scored
     */
    private ArrayList<Double> getTimeOfGoals() {
        ArrayList<Double> timeOfGoals = new ArrayList<>();

        int numberOfGoals = ctx.read("$.TickMarks.length()");

        Double goalTime;
        //saves are stored as well in the TickMarks array, so eventType makes sure only goals are returned
        String eventType;

        for (int i = 0; i < numberOfGoals; i++) {

            eventType = ctx.read("$.TickMarks[" + i + "].Type", String.class);
            if (eventType.equals("Team0Goal") || eventType.equals("Team1Goal")) {
                goalTime = ctx.read("$.TickMarks[" + i + "].Time", Double.class);
                timeOfGoals.add(goalTime);
            }
        }

        return timeOfGoals;
    }

    /**
     * Reads Match propertiees and generates MatchDTO
     * Information not accurate, refactor to read playerInfo from frames
     *
     * @return MatchDTO created from the analysed Json file
     * @throws FileServiceException if the file couldn't be parsed
     */
    private MatchDTO readProperties() throws FileServiceException {
        LOG.trace("Called - readProperties");

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


}

