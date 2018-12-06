package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dao.exception.MatchAlreadyExistsException;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Main Window Controller.
 *
 * @author Bernhard Bayer
 */
@Component
public class MainWindowController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    @Autowired
    private MatchDetailController matchdetailController;
    private ExecutorService executorService;
    private ReplayService replayService;
    private JsonParseService jsonParseService;
    private MatchService matchService;
    private PlayerService playerService;

    @FXML
    private TableView<MatchDTO> tableViewMatches;
    @FXML
    private TableColumn<MatchDTO, LocalDateTime> tableColumnMatchDate;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnMatchType;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnPlayersBlue;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnPlayersRed;

    //Player Tab
    @FXML
    private TableView<PlayerDTO> tableViewPlayers;
    @FXML
    private TableColumn<PlayerDTO, String> tableColumnPlayerName;


    public MainWindowController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService, ReplayService replayService, JsonParseService jsonParseService, MatchService matchService, PlayerService playerService) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
        this.replayService = replayService;
        this.jsonParseService = jsonParseService;
        this.matchService = matchService;
        this.playerService = playerService;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {

        setupMatchTable();
        setupPlayerTable();
        updateMatchTable();
        updatePlayerTable();

    }

    /**
     * Opens a new Match Detail window.
     *
     * @param actionEvent Actionevent from the button
     */
    public void onMatchdetailsButtonClicked(ActionEvent actionEvent) {

        LOG.info("Match Details button clicked");
        LOG.debug("Opening Match Details window");

        Stage matchdetailsStage = new Stage();
        // setup application
        matchdetailsStage.setTitle("Matchdetails");
        matchdetailsStage.setWidth(1024);
        matchdetailsStage.setHeight(768);
        matchdetailsStage.centerOnScreen();
        matchdetailsStage.setOnCloseRequest(event -> {
            LOG.debug("Match Details window closed");
        });


        try {
            matchdetailsStage.setScene(new Scene(springFXMLLoader.load("/fxml/matchdetail.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading Match Details fxml failed: " + e.getMessage());
        }

        // load match details for the new window
        matchdetailController.loadBasicMatchData(tableViewMatches.getSelectionModel().getSelectedItem());
        // show application
        matchdetailsStage.show();
        matchdetailsStage.toFront();
        LOG.debug("Opening Match Details window complete");

    }


    /**
     * Method that opens a FileChooser when the actionEvent occurs
     * Lets the user chose .replay files
     * <p>
     * The chosen file gets sent to service layer, to parse it
     *
     * @param actionEvent Actionevent from the button
     */
    public void onUploadReplayButtonClicked(ActionEvent actionEvent) {
        LOG.info("Image Chooser clicked");
        LOG.trace("Called - onUploadMatchButtonClicked");

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterREPLAY = new FileChooser.ExtensionFilter("Replay files (*.replay)", "*.REPLAY");
        fileChooser.getExtensionFilters().add(extFilterREPLAY);


        //Show open file dialog
        File inputFile = fileChooser.showOpenDialog(null);
        if (inputFile == null) {
            LOG.info("File selection cancelled");
            return;
        }

        executorService.submit(() -> {
            try {
                File json = replayService.parseReplayFileToJson(inputFile);
                MatchDTO matchDto = jsonParseService.parseMatch(json);
                LOG.debug("jsonParseFinished");
                for (MatchPlayerDTO mpdto : matchDto.getPlayerData()) {
                    playerService.createPlayer(mpdto.getPlayerDTO());
                }
                LOG.debug("All players created");
                matchService.createMatch(matchDto);
                LOG.debug("match created");
                Platform.runLater(() -> {
                    updateMatchTable();
                    updatePlayerTable();
                });
            } catch (FileServiceException e) {
                LOG.error("Caught File Service Exception");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (PlayerServiceException e) {
                LOG.error("Caught PlayerServiceException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (PlayerValidationException e) {
                LOG.error("Caught PlayerValidationException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (MatchServiceException e) {
                LOG.error("Caught MatchServiceException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (MatchValidationException e) {
                LOG.error("Caught MatchValidationException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (MatchAlreadyExistsException e) {
                LOG.error("Caught MatchAlreadyExistsException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            }
        });

    }

    /**
     * Deletes selected players in the list.
     *
     * @param actionEvent Action event from the button
     */
    public void onDeletePlayerButtonClicked(ActionEvent actionEvent) {
        LOG.info("Delete player button clicked");
        LOG.trace("Called - onDeletePlayerButtonClicked");

        ObservableList<PlayerDTO> selectedPlayers;
        selectedPlayers = tableViewPlayers.getSelectionModel().getSelectedItems();

        if (selectedPlayers.isEmpty()) {
            showErrorMessage("No player selected");
            return;
        }

        //new list for delete method in service layer
        List<PlayerDTO> playersToDelete = new LinkedList<>();
        //get player names for info message
        String playerNames = "";
        int counter = 0;

        for (PlayerDTO p : selectedPlayers) {
            if (counter != 0) {
                playerNames += ", ";
            }
            playerNames += p.getName();
            playersToDelete.add(p);
            counter++;
        }
        //let the user confirm the deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Players");
        alert.setContentText("Are you sure you want to delete the following players? \n" + playerNames);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            try {
                playerService.deletePlayers(playersToDelete);
            } catch (PlayerServiceException e) {
                LOG.error("Caught PlayerServiceException");
                showErrorMessage("Error while deleting player(s).");
            } catch (PlayerValidationException e) {
                LOG.error("Caught PlayerValidationException");
                showErrorMessage("List of players to be deleted might be empty.");
            }
            updatePlayerTable();
        }
    }


    /**
     * Method to show a simple Error Alert to the user
     *
     * @param errorMessage The String containing the message displayed
     */
    private void showErrorMessage(String errorMessage) {

        LOG.trace("Called - showErrorMessage");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Wrong input error");
        alert.setHeaderText("Error");
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }


    /**
     * Method to update the Match Table
     * Calls the showErrorMessage if an Exception occurs
     */
    private void updateMatchTable() {
        try {
            ObservableList<MatchDTO> observableMatches = FXCollections.observableArrayList(matchService.getMatches());
            SortedList<MatchDTO> sortedMatches = new SortedList<>(observableMatches);
            sortedMatches.comparatorProperty().bind(tableViewMatches.comparatorProperty());
            tableViewMatches.getSortOrder().add(tableColumnMatchDate);
            tableViewMatches.setItems(sortedMatches);
        } catch (MatchServiceException e) {
            LOG.error("Caught MatchServiceException {} ", e.getMessage());
            showErrorMessage(e.getMessage());
        }
    }

    /**
     * Loads the players into the player table
     * Calls the showErrorMessage if an Exception occurs
     */
    protected void updatePlayerTable() {
        try {
            ObservableList<PlayerDTO> observablePlayers = FXCollections.observableArrayList(playerService.getPlayers());

            tableViewPlayers.setItems(observablePlayers);
        } catch (PlayerServiceException e) {
            LOG.error("Caught PlayerServiceException {} ", e.getMessage());
            showErrorMessage(e.getMessage());
        }
    }

    /**
     * Helper Method to setup up the Player Table Column
     */
    private void setupPlayerTable() {
        tableColumnPlayerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableViewPlayers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableColumnPlayerName.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Helper Method to setup up the Match Table Columns
     */
    private void setupMatchTable() {
        tableColumnMatchDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        tableColumnPlayersRed.setCellValueFactory(new PropertyValueFactory<>("teamRedPlayers"));
        tableColumnPlayersBlue.setCellValueFactory(new PropertyValueFactory<>("teamBluePlayers"));

        tableColumnMatchDate.setSortType(TableColumn.SortType.DESCENDING);
        tableColumnMatchDate.setSortable(true);
        tableViewMatches.sort();
    }


}
