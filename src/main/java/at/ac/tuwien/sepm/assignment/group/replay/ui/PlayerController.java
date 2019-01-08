package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
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
 * Player Tab Page Controller.
 *
 * @author Bernhard Bayer
 */
@Component
public class PlayerController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private ExecutorService executorService;
    private PlayerService playerService;
    private PlayerDetailController playerDetailController;

    @FXML
    private TableView<PlayerDTO> tableViewPlayers;
    @FXML
    private TableColumn<PlayerDTO, String> tableColumnPlayerName;

    public PlayerController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService, PlayerService playerService, PlayerDetailController playerDetailController) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
        this.playerService = playerService;
        this.playerDetailController = playerDetailController;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    private void initialize() {

        setupPlayerTable();
        updatePlayerTable();

    }

    @FXML
    private void onPlayerDetailsButtonClicked() {
        LOG.info("Player Details button clicked");

        Stage playerDetailStage = new Stage();
        // setup application
        playerDetailStage.setTitle("Player Details");
        playerDetailStage.setWidth(1024);
        playerDetailStage.setHeight(768);
        playerDetailStage.centerOnScreen();
        playerDetailStage.setOnCloseRequest(event -> {
            LOG.debug("Player Details window closed");
        });


        try {
            playerDetailStage.setScene(new Scene(springFXMLLoader.load("/fxml/playerDetail.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading Player Detail fxml failed", e);
        }

        if (tableViewPlayers.getSelectionModel().getSelectedItems().size() == 1) {
            playerDetailController.loadPlayer(tableViewPlayers.getSelectionModel().getSelectedItem());
            playerDetailStage.toFront();
            playerDetailStage.showAndWait();
        } else {
            AlertHelper.showErrorMessage("you must select exactly one player");
        }

    }

    /**
     * Deletes selected players in the list.
     */
    @FXML
    private void onDeletePlayerButtonClicked() {
        LOG.info("Delete player button clicked");
        LOG.trace("Called - onDeletePlayerButtonClicked");

        ObservableList<PlayerDTO> selectedPlayers;
        selectedPlayers = tableViewPlayers.getSelectionModel().getSelectedItems();

        if (selectedPlayers.isEmpty()) {
            AlertHelper.showErrorMessage("No player selected");
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
        Optional<ButtonType> result = AlertHelper.alert(Alert.AlertType.CONFIRMATION,"Delete Players",null,"Are you sure you want to delete the following players? \n" + playerNames);

        if (result.get() == ButtonType.OK) {
            try {
                playerService.deletePlayers(playersToDelete);
            } catch (PlayerServiceException e) {
                LOG.error("Caught PlayerServiceException");
                AlertHelper.showErrorMessage("Error while deleting player(s).");
            } catch (PlayerValidationException e) {
                LOG.error("Caught PlayerValidationException");
                AlertHelper.showErrorMessage("List of players to be deleted might be empty.");
            }
            updatePlayerTable();
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
            AlertHelper.showErrorMessage(e.getMessage());
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

}
