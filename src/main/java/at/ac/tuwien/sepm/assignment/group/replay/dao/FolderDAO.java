package at.ac.tuwien.sepm.assignment.group.replay.dao;

import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.CouldNotCreateFolderException;
import at.ac.tuwien.sepm.assignment.group.replay.dao.impl.UserFolderDAO;
import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.FilePersistenceException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;

import java.io.File;

public interface FolderDAO {

    /**
     * Method that sets up a Directory
     *
     * @return the created Directory
     */
    File setupDirectory(String folder) throws CouldNotCreateFolderException;

    /**
     * Method that copies a .replay file into the {@link UserFolderDAO#fileDirectory} folder.
     * Assigns a unique name to the copied file.
     *
     * @return The file after it has been copied.
     * @throws FilePersistenceException if the file is not a .replay file
     */
    File copyReplayFile(File file) throws FilePersistenceException;


    /**
     * Method that creates a Json File with the same base name as the given replayFile
     * File is created inside the FileDirectory
     *
     * @return a File containing the Path to the created Json File
     */
    File createJsonFile(File replayFile);


    /**
     * Method that returns the Directory the parser is saved in
     *
     * @return a File containing the Folder the Parser is extracted to
     */
    File getParserDirectory();

    /**
     * Method that returns the Directory the replay files are copied to
     *
     * @return a File containing the Folder the replay files are copied to
     */
    File getFileDirectory();

    /**
     * Method that returns the Directory the heatmap images are copied to
     *
     * @return a File containing the Folder the heatmap images are copied to
     */
    File getHeatmapDirectory();

    /**
     * Method that returns the File with the Path to the Parser.
     * Has to call setupParser if the Parser has not been set up yet.
     *
     * @param necesarryFiles The necesarry Files for the Parser
     * @return a File containing the executable Path for the .replay File Parser
     * @throws FilePersistenceException if parser Setup fails
     */
    File getParser(String[] necesarryFiles) throws FilePersistenceException;

    /**
     * Deletes a given File
     * File has to be .json or .replay
     * @param file the file to delete
     * @throws FilePersistenceException if the file could not be deleted or the File has the wrong filetype
     */
    void deleteFile(File file) throws FilePersistenceException;

    /**
     * Saves the Heatmaps from the MatchDTO in the heatmaps directory
     * @param matchDTO heatmaps to save for a Match
     */
    void saveHeatmaps(MatchDTO matchDTO);

    /**
     * Gets the Heatmap Images from the heatmaps directory and saves it to the MatchDTO
     * @param matchDTO the match to get the heatmap
     */
    void getHeatmaps(MatchDTO matchDTO);

}