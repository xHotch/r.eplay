package at.ac.tuwien.sepm.assignment.group.replay.service;


import at.ac.tuwien.sepm.assignment.group.replay.dto.HeatmapDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;

import java.io.File;

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
     * Get the calculate the heatmap for a player
     * @param matchPlayerDTO the player data
     * @return HeatmapDTO with the dataset for the heatmap
     */
    HeatmapDTO calculateHeatmap(MatchPlayerDTO matchPlayerDTO);
}
