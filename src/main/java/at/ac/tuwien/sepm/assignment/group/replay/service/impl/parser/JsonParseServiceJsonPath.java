package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
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

    private RigidBodyParser rigidBodyParser;
    private PlayerInformationParser playerInformationParser;
    private GameInformationParser gameInformationParse;
    private CarInformationParser carInformationParser;
    private BallInformationParser ballInformationParser;


    public JsonParseServiceJsonPath(RigidBodyParser rigidBodyParser, PlayerInformationParser playerInformationParser, GameInformationParser gameInformationParse, CarInformationParser carInformationParser, BallInformationParser ballInformationParser) {
        this.rigidBodyParser = rigidBodyParser;
        this.playerInformationParser = playerInformationParser;
        this.gameInformationParse = gameInformationParse;
        this.carInformationParser = carInformationParser;
        this.ballInformationParser = ballInformationParser;
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

                jFile = jsonFile;
            } catch (IOException e) {
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }

        }

        parseFrames();
        LOG.debug("asdf");
        //Todo parse Player information from Frames not Properties

        carInformationParser.getPlayerCarMap();

        return readProperties();
    }

    /**
     * Method that loops through frames and actorupdates.
     * Calls other classes to parse information from the json, depending on the classtype of the actor
     *
     * @throws FileServiceException if File could not be parsed
     */
    private void parseFrames() throws FileServiceException {
        LOG.trace("Called - parseFrames");
        //Map that contains the ids and Classnames from actors.
        //Actors can referene each other, e.g. a car references a player by setting ['Engine.Pawn:PlayerReplicationInfo'] to the ActorID of the player
        HashMap<Integer, String> actors = new HashMap<>();

        //list that stores time when goals were scored, in order to pause the game afterwards
        gameInformationParse.setTimeOfGoals();

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
                gamePaused = gameInformationParse.pauseGameIfGoalWasScored(frameTime);}

                for (int currentActorUpdateNr = 0; currentActorUpdateNr < actorUpdateCount; currentActorUpdateNr++) {

                    int actorId = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].Id", Integer.class);

                    //New Actors have field ClassName. Check if it is a new actor
                    String newActor = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].ClassName");
                    if (newActor != null) {
                        actors.put(actorId, newActor);
                        LOG.debug("New Actor found at frame {}, actorupdate {}", currentFrame, currentActorUpdateNr);
                    }


                    String className = actors.get(actorId);

                    //resume game if countdown equals 0 (is shown after a goal before the game resumes)
                    if (gamePaused) {
                        gamePaused = gameInformationParse.resumeGameIfCountdownIsZero(frame, currentActorUpdateNr);
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
                matchPlayer.setTeam(TeamSide.getById(ctx.read("$.Properties.PlayerStats[" + i + "].Team")).get());

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

