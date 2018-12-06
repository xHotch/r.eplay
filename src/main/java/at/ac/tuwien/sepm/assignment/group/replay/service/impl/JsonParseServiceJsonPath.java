
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
            LOG.error("Can't parse null");
            throw new FileServiceException("Can't parse null");
        }
        String extension = FilenameUtils.getExtension(jsonFile.getName());
        if (!extension.equals("json")) {
            LOG.error("wrong file type: " + extension);
            throw new FileServiceException("wrong file type: " + extension);
        }
        if (!jsonFile.equals(jFile) || ctx == null) {
            try {
                Configuration conf = Configuration.defaultConfiguration();
                Configuration conf2 = conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
                ctx = JsonPath.using(conf2).parse(jsonFile);
                jFile = jsonFile;
            } catch (IOException e) {
                LOG.error("Could not parse replay file {}", jsonFile.getAbsolutePath());
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }

        }

        HashMap<Integer,String> actors = new HashMap<>();

        ArrayList<BallInformation> ballInformations = new ArrayList<>();

        int frameCount = ctx.read("$.Frames.length()");

        LOG.debug("Match framecount : {}",frameCount);

        try {
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
                            //parseCarInformation(i,j);
                        case "TAGame.PRI_TA" :
                            //parsePlayerInformation(i,j);


                    }

                    LOG.debug(className);
                }
            }
        } catch (Exception e){
            LOG.error("error while parsing frames", e);
        }

        //todo implement
        generateBallStatistic(ballInformations);

        return readProperties();
    }

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
                    LOG.error("A player has no id");
                    throw new FileServiceException("A player has no id");
                }
                playerDTO.setPlatformID(id);
                playerList.add(matchPlayer);
            }
            match.setPlayerData(playerList);


        } catch (Exception e) {
            LOG.error("Error during parsing" + e.getMessage());
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

