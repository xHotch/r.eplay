package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.BallStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.BoostStatistic;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic.PlayerStatistic;
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
    private MatchDTO matchDTO;

    private RigidBodyParser rigidBodyParser;
    private PlayerInformationParser playerInformationParser;
    private GameInformationParser gameInformationParser;
    private CarInformationParser carInformationParser;
    private BallInformationParser ballInformationParser;
    private BoostInformationParser boostInformationParser;
    private PlayerStatistic playerStatistic;
    private BallStatistic ballStatistic;
    private BoostStatistic boostStatistic;
    private ReplayService replayService;
    private MatchService matchService;



    public JsonParseServiceJsonPath(RigidBodyParser rigidBodyParser, PlayerInformationParser playerInformationParser, GameInformationParser gameInformationParser, CarInformationParser carInformationParser, BallInformationParser ballInformationParser, BoostInformationParser boostInformationParser, PlayerStatistic playerStatistic, BallStatistic ballStatistic, BoostStatistic boostStatistic, ReplayService replayService, MatchService matchService) {
        this.rigidBodyParser = rigidBodyParser;
        this.playerInformationParser = playerInformationParser;
        this.gameInformationParser = gameInformationParser;
        this.carInformationParser = carInformationParser;
        this.ballInformationParser = ballInformationParser;
        this.boostInformationParser = boostInformationParser;
        this.playerStatistic = playerStatistic;
        this.ballStatistic = ballStatistic;
        this.boostStatistic = boostStatistic;
        this.replayService = replayService;
        this.matchService = matchService;
    }


    private VideoDTO parseVideo(File jsonFile) throws FileServiceException {
        LOG.trace("called - parseMatch");
        if (jsonFile == null) {

            throw new FileServiceException("Can't parse null");
        }
        String extension = FilenameUtils.getExtension(jsonFile.getName());
        if (!extension.equals("json")) {
            throw new FileServiceException("wrong file type: " + extension);
        }

            try {
                Configuration conf = Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
                ctx = JsonPath.using(conf).parse(jsonFile);

                playerInformationParser.setCtx(ctx);
                playerInformationParser.setUp();
                rigidBodyParser.setCtx(ctx);
                carInformationParser.setCtx(ctx);
                carInformationParser.setup();
                ballInformationParser.setCtx(ctx);
                ballInformationParser.setup();
                boostInformationParser.setCtx(ctx);
                boostInformationParser.setup();

                jFile = jsonFile;
            } catch (IOException e) {
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }



        LOG.debug("Start Parse");
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setFrames(parseVideoFrames());
        LOG.debug("End Parse");
        return videoDTO;
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

            try {
                Configuration conf = Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
                ctx = JsonPath.using(conf).parse(jsonFile);

                playerInformationParser.setCtx(ctx);
                playerInformationParser.setUp();
                rigidBodyParser.setCtx(ctx);
                gameInformationParser.setCtx(ctx);
                carInformationParser.setCtx(ctx);
                carInformationParser.setup();
                ballInformationParser.setCtx(ctx);
                ballInformationParser.setup();
                boostInformationParser.setCtx(ctx);
                boostInformationParser.setup();

                jFile = jsonFile;
            } catch (IOException e) {
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }

        LOG.debug("Start Parse");
        matchDTO = readProperties();
        parseFrames();
        LOG.debug("End Parse");
        //playerInformationParser.setActorId(matchDTO.getPlayerData());
        List<MatchPlayerDTO> players = playerInformationParser.getMatchPlayer();
        if (matchDTO.getTeamSize() * 2 != players.size()) {
            throw new FileServiceException("Wrong number of players in the game");
        }
        matchDTO.setPlayerData(players);
        LOG.debug("Start  Calculate");
        calculate(matchDTO);
        LOG.debug("End  Calculate");
        return matchDTO;
    }

    @Override
    public VideoDTO getVideo(MatchDTO matchDTO) throws FileServiceException{

        //get json from .replay
        File jsonFile = null;
        try {
            jsonFile=replayService.parseReplayFileToJson(matchDTO.getReplayFile());
            VideoDTO videoDTO = parseVideo(jsonFile);
            videoDTO.setActorIds(playerInformationParser.getPlatformIdToActorId());
            videoDTO.setCarActorIds(carInformationParser.getPlayerCarMap());
            videoDTO.setPlayerToCarAndTimeMap(carInformationParser.getPlayerToCarAndFrameTimeMap());

            return videoDTO;
        } finally {
            if (jsonFile!=null) {
                matchService.deleteFile(jsonFile);
            }
        }
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
        gameInformationParser.setTimeOfGoals();

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
                gamePaused = gameInformationParser.pauseGameIfGoalWasScored(frameTime);}



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
                        gamePaused = gameInformationParser.resumeGameIfCountdownIsZero(frame, currentActorUpdateNr);
                    }
                    switch (className) {
                        case "TAGame.Ball_TA":
                            ballInformationParser.parse(currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;
                        case "TAGame.Car_TA":
                            carInformationParser.parse(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;

                        case "TAGame.PRI_TA":
                            playerInformationParser.parse(actorId, currentFrame, currentActorUpdateNr);

                            break;

                        case "TAGame.Team_Soccar_TA":
                            playerInformationParser.parseTeam(actorId, currentFrame, currentActorUpdateNr);
                            break;
                        case "TAGame.GRI_TA":
                            //parseMatchInformation
                            //e.g ['ProjectX.GRI_X:ReplicatetdGamePlaylist'] -> MatchType id
                            break;

                        case "TAGame.CarComponent_Boost_TA":
                            boostInformationParser.parse(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;

                        case "TAGame.VehiclePickup_Boost_TA":
                            boostInformationParser.parseBoostPad(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused, actorUpdateCount);
                            break;

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
     * Method that loops through frames and actorupdates.
     * Calls other classes to parse information from the json, depending on the classtype of the actor
     *
     * @throws FileServiceException if File could not be parsed
     */
    private List<FrameDTO> parseVideoFrames() throws FileServiceException {

        ArrayList<FrameDTO> frameDTOS = new ArrayList<>();
        LOG.trace("Called - parseVideoFrames");

        //Map that contains the ids and Classnames from actors.
        //Actors can referene each other, e.g. a car references a player by setting ['Engine.Pawn:PlayerReplicationInfo'] to the ActorID of the player
        HashMap<Integer, String> actors = new HashMap<>();


        //pause game at the beginning
        boolean gamePaused = true;

        //Frames for video
        VideoDTO videoDTO = new VideoDTO();

        try {

            int frameCount = ctx.read("$.Frames.length()");

            LOG.debug("Match framecount : {}", frameCount);
            for (int currentFrame = 0; currentFrame < frameCount; currentFrame++) {

                String frame = "$.Frames[" + currentFrame + "]";
                int actorUpdateCount = ctx.read(frame + ".ActorUpdates.length()");
                LOG.debug("Frame {} has {} ActorUpdates", currentFrame, actorUpdateCount);



                double frameTime = ctx.read(frame + ".Time", Double.class);
                double frameDelta = ctx.read(frame + ".Delta", Double.class);




                //Frames for video
                //videoDTO.addFrame(frameTime);
                FrameDTO frameDTO = new FrameDTO(frameTime);


                int i = 0;
                for (int currentActorUpdateNr = 0; currentActorUpdateNr < actorUpdateCount; currentActorUpdateNr++) {

                    int actorId = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].Id", Integer.class);

                    //New Actors have field ClassName. Check if it is a new actor
                    String newActor = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].ClassName");
                    if (newActor != null) {
                        actors.put(actorId, newActor);
                        LOG.debug("New Actor found at frame {}, actorupdate {}", currentFrame, currentActorUpdateNr);
                    }


                    String className = actors.get(actorId);


                    switch (className) {
                        case "TAGame.Ball_TA":
                            ballInformationParser.parseVideoFrame(currentFrame, currentActorUpdateNr, frameDTO, gamePaused);
                            i++;
                            break;
                        case "TAGame.Car_TA":
                            carInformationParser.parseVideoFrame(actorId, currentFrame, currentActorUpdateNr, frameDTO, gamePaused, frameTime);
                            i++;
                            break;
                        case "TAGame.PRI_TA":
                            playerInformationParser.parse(actorId, currentFrame, currentActorUpdateNr);

                            break;
                        case "TAGame.CarComponent_Boost_TA":
                            boostInformationParser.parse(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused);
                            break;
                        case "TAGame.VehiclePickup_Boost_TA":
                            boostInformationParser.parseBoostPad(actorId, currentFrame, currentActorUpdateNr, frameTime, frameDelta, gamePaused, actorUpdateCount);
                            break;
                        default:
                            break;
                    }


                    LOG.debug(className);
                }
                if (i != 0){
                    frameDTOS.add(frameDTO);
                }
            }
        } catch (Exception e) {
            throw new FileServiceException("Exception while parsing frames", e);
        }
        return frameDTOS;
    }


    /**
     * Calculates all statistics to be save in the database
     *
     * @param matchDTO the match data wto save the statistics
     */
    private void calculate(MatchDTO matchDTO) {
        LOG.trace("Called - calculate");
        playerStatistic.calculate(matchDTO.getPlayerData(),carInformationParser.getRigidBodyListPlayer(),ballInformationParser.getRigidBodyInformations()); //TODO link actorID to matchplayer
        ballStatistic.calculate(matchDTO, ballInformationParser.getRigidBodyInformations(), ballInformationParser.getHitTimes());
        boostStatistic.calculate(matchDTO.getPlayerData(), ballInformationParser.getRigidBodyInformations(), boostInformationParser.getBoostPadMap());
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
            /*List<MatchPlayerDTO> playerList = new ArrayList<>();
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
            */

        } catch (Exception e) {
            throw new FileServiceException(e.getMessage(), e);
        }
        return match;
    }


}

