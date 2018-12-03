package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the Match Detail window
 * @author Bernhard Bayer
 */
@Component
public class MatchdetailController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public MatchdetailController() {
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
            if(player.getTeam() == 0){
                blueGoals += player.getGoals();

                playerListBlue.add(player);
            }
            if(player.getTeam() == 1){
                redGoals += player.getGoals();

                playerListRed.add(player);
            }
        }

        this.labelTeamRed.setText("Goals: " + redGoals);
        this.labelTeamBlue.setText("Goals: " + blueGoals);

        tableTeamBlue.setItems(playerListBlue);
        tableTeamRed.setItems(playerListRed);
    }

    /**
     * Helper Method to setup up the Player Table Columns
     */
    private void setUpPlayerTable() {
        playerNameBlue.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, String>("name"));
        playerGoalsBlue.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("goals"));
        playerShotsBlue.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("shots"));
        playerSavesBlue.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("saves"));
        playerAssistsBlue.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("assists"));

        playerNameRed.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, String>("name"));
        playerGoalsRed.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("goals"));
        playerShotsRed.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("shots"));
        playerSavesRed.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("saves"));
        playerAssistsRed.setCellValueFactory(new PropertyValueFactory<MatchPlayerDTO, Integer>("assists"));

    }
}
