
package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import com.jayway.jsonpath.*;
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

    //Strings
    private String rigidBody = "['TAGame.RBActor_TA:ReplicatedRBState']";
    private String rigidBodyPosition = rigidBody + ".Position";




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
        HashMap<Integer,String> actors = new HashMap<>();

        //Create List that stores calculated ballInformations. is used to generate Ball statistics
        ArrayList<BallInformation> ballInformations = new ArrayList<>();

        try {

            int frameCount = ctx.read("$.Frames.length()");

            LOG.debug("Match framecount : {}",frameCount);
            for (int i = 0; i<frameCount; i++){

                String frame = "$.Frames["+i+"]";
                int actorUpdateCount = ctx.read(frame + ".ActorUpdates.length()");
                LOG.debug("Frame {} has {} ActorUpdates",i,actorUpdateCount);


                double frameTime = ctx.read(frame + ".Time");
                double frameDelta;

                //Cast necesarry because jsonPath will return an int if a value is 0 and throw an error
                try {
                    frameDelta = ctx.read(frame + ".Delta");
                } catch (Exception e){
                    frameDelta=0.0;
                }

                for (int j = 0; j<actorUpdateCount; j++){

                    int actorId = ctx.read(frame + ".ActorUpdates["+j+"].Id");

                    //New Actors have field ClassName. Check if it is a new actor
                    String newActor = ctx.read(frame + ".ActorUpdates["+j+"].ClassName");
                    if(newActor!= null){
                        actors.put(actorId,newActor);
                        LOG.debug("New Actor found at frame {}, actorupdate {}", i,j);
                    }


                    String className = actors.get(actorId);


                    switch (className){
                        case "TAGame.Ball_TA" :
                            ballInformations.add(parseBallInformation(i,j,frameTime,frameDelta));
                            break;
                        case "TAGame.Car_TA" :
                            //parseCarInformation(i,j,frameTime,frameDelta);
                            break;
                        case "TAGame.PRI_TA" :
                            //parsePlayerInformation(i,j);
                            break;
                        case "TAGamee.GRI_TA" :
                            //parseMatchInformation
                            //e.g ['ProjectX.GRI_X:ReplicatetdGamePlaylist'] -> MatchType id

                        default:
                            //Information not relevant for our project
                            break;

                    }

                    LOG.debug(className);
                }
            }
        } catch (Exception e){
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
    private MatchDTO readProperties() throws FileServiceException{
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

    private BallInformation parseBallInformation(int frameId,int actorUpdateId, double frameTime, double frameDelta){
        LOG.trace("Called - parseBallInformation");
        BallInformation ballInformation = new BallInformation();
        LinkedHashMap<String, Object> position = ctx.read("$.Frames["+frameId+"].ActorUpdates["+actorUpdateId+"]." + rigidBodyPosition);

        ballInformation.setPosition(position);
        ballInformation.setFrameTime(frameTime);
        ballInformation.setFrameDelta(frameDelta);

        LOG.debug(String.valueOf(position.size()));
        return ballInformation;
    }


    //todo Refactor in seperate Statistic Class
    private void generateBallStatistic(List<BallInformation> ballInformations){
        LOG.debug("Match has {} ballinformations", ballInformations.size());
    }
}

