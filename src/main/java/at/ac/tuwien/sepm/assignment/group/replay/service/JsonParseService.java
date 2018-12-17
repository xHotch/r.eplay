package at.ac.tuwien.sepm.assignment.group.replay.service;


import at.ac.tuwien.sepm.assignment.group.replay.dto.HeatmapDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;

import java.io.File;
import java.util.Map;

/**
 * Service Interface that parses .json files to our dtos
 *
 * @author Markus Kogelbauer
 */
public interface JsonParseService {

    /**
     * Parses the given json-file into a match-dto
     *
     * Calls other methods to parse further information from the json file
     *
     * @param jsonFile The file that is parsed
     * @return The match parsed from the input-file
     * @throws FileServiceException if the file couldn't be parsed
     */
    MatchDTO parseMatch(File jsonFile) throws FileServiceException;

    /**
     * Get the calculate the heatmaps for the players and the ball
     *
     * @return Map of HeatmapDTO with the image of the heatmap, for player the key is the name and for the ball "ball"
     */
    Map<String, HeatmapDTO> calculateHeatmap();
}
