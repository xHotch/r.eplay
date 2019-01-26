package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.AvgStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Player Tab Page Controller.
 *
 * @author Bernhard Bayer
 */
@Component
public class PlayerController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PlayerService playerService;

    @FXML
    private TableView<PlayerDTO> tableViewPlayers;
    @FXML
    private TableColumn<PlayerDTO, String> tableColumnPlayerName;

    @FXML
    private ChoiceBox<MatchType> typChoiceBox;
    @FXML
    private Text txtWins;
    @FXML
    private Text txtLosses;
    @FXML
    private Text txtGoals;
    @FXML
    private Text txtAssists;
    @FXML
    private Text txtSaves;
    @FXML
    private Text txtShots;
    @FXML
    private Text txtScore;
    @FXML
    private Text txtSpeed;
    @FXML
    private Text txtBoostpad;
    @FXML
    private Text txtBoost;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    private void initialize() {
        setupPlayerTable();
        updatePlayerTable();

        typChoiceBox.getItems().addAll(MatchType.RANKED1V1, MatchType.RANKED2V2, MatchType.RANKED3V3);
        typChoiceBox.getSelectionModel().selectLast();
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
            AlertHelper.showErrorMessage("Kein Spieler ausgewählt");
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
        Optional<ButtonType> result = AlertHelper.alert(Alert.AlertType.CONFIRMATION,"Lösche Spieler",null,"Bist du dir sicher, dass du die folgenden Spieler löschen möchtest? \n" + playerNames);

        if (result.get() == ButtonType.OK) {
            try {
                playerService.deletePlayers(playersToDelete);
            } catch (PlayerServiceException e) {
                LOG.error("Caught PlayerServiceException");
                AlertHelper.showErrorMessage("Fehler beim Löschen der/des Spieler/s.");
            } catch (PlayerValidationException e) {
                LOG.error("Caught PlayerValidationException");
                AlertHelper.showErrorMessage("Liste der zu löschenden Spieler könnte leer sein.");
            }
            updatePlayerTable();
            deletePlayerTexts();
        }
    }

    /**
     * Loads the players into the player table
     * Calls the showErrorMessage if an Exception occurs
     */
    void updatePlayerTable() {
        try {
            ObservableList<PlayerDTO> observablePlayers = FXCollections.observableArrayList(playerService.getPlayers());

            tableViewPlayers.setItems(observablePlayers);
        } catch (PlayerServiceException e) {
            LOG.error("Caught PlayerServiceException", e);
            AlertHelper.showErrorMessage("Fehler beim Laden der Spieler");
        }
    }

    /**
     * Helper Method to setup up the Player Table Column
     */
    private void setupPlayerTable() {
        tableColumnPlayerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableViewPlayers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewPlayers.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> showPlayerDetails(newValue));
        tableColumnPlayerName.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * shows the player details for a selected player on the right side of the table
     *
     * @param selectedPlayer player that is currently selected
     */
    private void showPlayerDetails(PlayerDTO selectedPlayer) {
        LOG.info("Show Player Details Button clicked");
        LOG.trace("called - onShowPlayerDetailsButtonClicked");
        if (selectedPlayer != null) {
            updatePlayerDetails(selectedPlayer, typChoiceBox.getSelectionModel().getSelectedItem());
            ChangeListener<MatchType> changeListener = (observable, oldType, newType) -> updatePlayerDetails(selectedPlayer, newType);
            typChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
        }
    }

    /**
     * updates player details text depending on the selected player
     *
     * @param selectedPlayer player to be updated
     */
    private void updatePlayerDetails(PlayerDTO selectedPlayer, MatchType matchType) {
        deletePlayerTexts();

        try {
            AvgStatsDTO avgStatsDTO = playerService.getAvgStats(selectedPlayer, matchType);
            txtWins.setText("" + avgStatsDTO.getWins());
            txtLosses.setText("" + avgStatsDTO.getLosses());
            txtGoals.setText("" + String.format("%.2f", avgStatsDTO.getGoals()));
            txtAssists.setText("" + String.format("%.2f", avgStatsDTO.getAssists()));
            txtSaves.setText("" + String.format("%.2f", avgStatsDTO.getSaves()));
            txtShots.setText("" + String.format("%.2f", avgStatsDTO.getShots()));
            txtScore.setText("" + String.format("%.2f", avgStatsDTO.getScore()));
            txtSpeed.setText("" + String.format("%.2f", avgStatsDTO.getSpeed()));
            txtBoostpad.setText("" + String.format("%.2f", avgStatsDTO.getBoostpads()));
            txtBoost.setText("" + String.format("%.2f", avgStatsDTO.getBoost()));
        } catch (PlayerServiceException e) {
            LOG.error("Caught PlayerServiceException {} ", e.getMessage());
            AlertHelper.showErrorMessage("Fehler beim Laden der Spieler Details");
        }
    }

    private void deletePlayerTexts() {
        txtWins.setText("");
        txtLosses.setText("");
        txtGoals.setText("");
        txtAssists.setText("");
        txtSaves.setText("");
        txtShots.setText("");
        txtScore.setText("");
        txtSpeed.setText("");
        txtBoostpad.setText("");
        txtBoost.setText("");
    }

}
