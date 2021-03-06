package at.ac.tuwien.sepm.assignment.group.replay.service;


import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;

import java.io.File;

/**
 * Service Interface that parses .replay files to .json files
 *
 * @author Philipp Hochhauser
 */
public interface ReplayService {


    /**
     * Method that parses a given .replay file into a .json file.
     * Needs to be run in seperate thread to not block main thread.
     *
     * @param replayFile The file that is parsed
     * @throws FileServiceException If the File could not be parsed
     */
    File parseReplayFileToJson(File replayFile) throws FileServiceException;

    /**
     * Method that copies a given .replay file into the folder where .replay files are persisted
     * Has to make sure the input file has the right format and is named uniquely.
     *
     * @param inputFile The file that is coppied
     * @throws FileServiceException If the File could not be copied
     */
    File copyReplayFile(File inputFile) throws FileServiceException;

}
