package at.ac.tuwien.sepm.assignment.group.replay.service;


import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.VideoDTO;
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
     * Parses the given MatchDTO into a VideoDTO
     *
     * @param matchDTO The match
     * @return The VideoDTO parsed from the replay file from the matchDTO
     * @throws FileServiceException if the file couldn't be parsed
     */
    VideoDTO getVideo(MatchDTO matchDTO) throws FileServiceException;
}
