package at.ac.tuwien.sepm.assignment.group.replay.service;


import at.ac.tuwien.sepm.assignment.group.universe.exceptions.ServiceException;

import java.io.File;

/**
 * Service Interface that parses .replay files to .json files
 * @author Philipp Hochhauser
 */
public interface ReplayService {


    /**
     * Method that parses a given .replay file into a .json file.
     * Has to make sure the input file has the right format and is named uniquely
     *
     * @param replayFile The file that is parsed
     * @throws ServiceException If the File could not be parsed
     */
    File parseReplayFileToJson (File replayFile) throws ServiceException;
}