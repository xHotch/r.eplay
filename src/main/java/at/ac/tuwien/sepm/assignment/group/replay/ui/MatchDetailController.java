package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Controller for the Match Detail window
 * @author Bernhard Bayer
 */
@Component
public class MatchDetailController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PlayerService playerService;
    private PlayerController playerController;

    @FXML
    private Label labelGameMode;
    @FXML
    private Label labelDate;
    @FXML
    private Label labelMatchDuration;
    @FXML
    private Label labelTeamRed;
    @FXML
    private Label labelTeamBlue;

    @FXML
    private TableView<MatchPlayerDTO> tableTeamBlue;
    @FXML
    private TableView<MatchPlayerDTO> tableTeamRed;

    @FXML
    private TableColumn<MatchPlayerDTO, String> playerNameBlue;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer> playerGoalsBlue;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer> playerShotsBlue;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer> playerSavesBlue;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer> playerAssistsBlue;
    @FXML
    private TableColumn<MatchPlayerDTO, String> playerNameRed;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer> playerGoalsRed;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer>playerShotsRed;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer>playerSavesRed;
    @FXML
    private TableColumn<MatchPlayerDTO, Integer>playerAssistsRed;

    public MatchDetailController(PlayerService playerService, PlayerController playerController) {
        this.playerService = playerService;
        this.playerController = playerController;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {

        setUpPlayerTable();
    }

    /**
     * Fills the basic Match data at startup.
     *
     * @param match to get the basic information from.
     */
    public void loadBasicMatchData(MatchDTO match) {

        int mode = match.getTeamSize();
        this.labelGameMode.setText(mode + "vs" + mode);

        this.labelDate.setText(match.getDateTime().toString());

        //TODO: version 2
        //this.label_MatchDuration.setText();

        List<MatchPlayerDTO> matchPlayers = match.getPlayerData();

        int redShots = 0;
        int blueShots = 0;

        int redAssists = 0;
        int blueAssists = 0;

        int redGoals = 0;
        int blueGoals = 0;

        ObservableList<MatchPlayerDTO> playerListBlue = FXCollections.observableArrayList();
        ObservableList<MatchPlayerDTO> playerListRed = FXCollections.observableArrayList();

        for (MatchPlayerDTO player:matchPlayers) {
            // team blue
            if(player.getTeam() == TeamSide.BLUE){
                blueGoals += player.getGoals();

                playerListBlue.add(player);
            }
            if(player.getTeam() == TeamSide.RED){
                redGoals += player.getGoals();

                playerListRed.add(player);
            }
        }

        this.labelTeamRed.setText("Goals: " + redGoals);
        this.labelTeamBlue.setText("Goals: " + blueGoals);

        tableTeamBlue.setItems(playerListBlue);
        tableTeamRed.setItems(playerListRed);
    }

    public void onSavePlayerButtonClicked() {
        ObservableList<MatchPlayerDTO> playersTeamBlue = tableTeamBlue.getSelectionModel().getSelectedItems();
        ObservableList<MatchPlayerDTO> playersTeamRed = tableTeamRed.getSelectionModel().getSelectedItems();

        if (playersTeamBlue.isEmpty() && playersTeamRed.isEmpty()) {
            AlertHelper.showErrorMessage("No player selected");
        } else {
            for (MatchPlayerDTO matchPlayer : playersTeamBlue) {
                try {
                    playerService.showPlayer(matchPlayer.getPlayerDTO());
                } catch (PlayerServiceException e) {
                    LOG.error("Caught PlayerServiceException", e);
                    AlertHelper.showErrorMessage(e.getMessage());
                }
            }

            for (MatchPlayerDTO matchPlayer : playersTeamRed) {
                try {
                    playerService.showPlayer(matchPlayer.getPlayerDTO());
                } catch (PlayerServiceException e) {
                    LOG.error("Caught PlayerServiceException", e);
                    AlertHelper.showErrorMessage(e.getMessage());
                }
            }
            playerController.updatePlayerTable();
        }
    }

    /**
     * Helper Method to setup up the Player Table Columns
     */
    private void setUpPlayerTable() {
        playerNameBlue.setCellValueFactory(new PropertyValueFactory<>("name"));
        playerGoalsBlue.setCellValueFactory(new PropertyValueFactory<>("goals"));
        playerShotsBlue.setCellValueFactory(new PropertyValueFactory<>("shots"));
        playerSavesBlue.setCellValueFactory(new PropertyValueFactory<>("saves"));
        playerAssistsBlue.setCellValueFactory(new PropertyValueFactory<>("assists"));

        playerNameRed.setCellValueFactory(new PropertyValueFactory<>("name"));
        playerGoalsRed.setCellValueFactory(new PropertyValueFactory<>("goals"));
        playerShotsRed.setCellValueFactory(new PropertyValueFactory<>("shots"));
        playerSavesRed.setCellValueFactory(new PropertyValueFactory<>("saves"));
        playerAssistsRed.setCellValueFactory(new PropertyValueFactory<>("assists"));

    }
}
