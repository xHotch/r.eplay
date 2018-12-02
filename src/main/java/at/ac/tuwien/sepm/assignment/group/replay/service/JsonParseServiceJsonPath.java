package at.ac.tuwien.sepm.assignment.group.replay.service;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.FileServiceException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
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
                ctx = JsonPath.parse(jsonFile);
                jFile = jsonFile;
            } catch (IOException e) {
                LOG.error("Could not parse replay file" + jsonFile.getAbsolutePath());
                throw new FileServiceException("Could not parse replay file" + jsonFile.getAbsolutePath());
            }

        }
        MatchDTO match = new MatchDTO();
        try {
            String dateTime = ctx.read("$.Properties.Date");
            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
            match.setDateTime(LocalDateTime.parse(dateTime, dtFormatter));
            match.setTeamSize(ctx.read("$.Properties.TeamSize"));
            match.setReadId(ctx.read("$.Properties.Id"));
            List<MatchPlayerDTO> playerList = new ArrayList<>();
            for (int i = 0; i < match.getTeamSize() * 2; i++) {
                MatchPlayerDTO matchPlayer = new MatchPlayerDTO();
                matchPlayer.setAssists(ctx.read("$.Properties.PlayerStats[" + i + "].Assists"));
                matchPlayer.setGoals(ctx.read("$.Properties.PlayerStats[" + i + "].Goals"));
                matchPlayer.setSaves(ctx.read("$.Properties.PlayerStats[" + i + "].Saves"));
                matchPlayer.setShots(ctx.read("$.Properties.PlayerStats[" + i + "].Shots"));
                matchPlayer.setScore(ctx.read("$.Properties.PlayerStats[" + i + "].Score"));
                matchPlayer.setName(ctx.read("$.Properties.PlayerStats[" + i + "].Name"));
                matchPlayer.setTeam(ctx.read("$.Properties.PlayerStats[" + i + "].Team"));
                long id = ctx.read("$.Properties.PlayerStats[" + i + "].OnlineID");
                if (id == 0) {
                    LOG.error("A player has no id");
                    throw new FileServiceException("A player has no id");
                }
                matchPlayer.setPlattformId(id);
                playerList.add(matchPlayer);
            }
            match.setPlayerData(playerList);


        } catch (Exception e) {
            LOG.error("Error during parsing" + e.getMessage());
            throw new FileServiceException(e.getMessage(), e);
        }
        return match;
    }

}
