package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamCompareDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Gabriel Aichinger
 */

@Controller
public class TeamMatchesController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private TeamCompareController teamCompareController;
    private TeamService teamService;
    private TeamDTO team1;
    private TeamDTO team2;

    private TeamCompareDTO teamCompareDTO;
    @FXML
    private TableView<MatchDTO> tableViewMatches;
    @FXML
    private TableColumn<MatchDTO, LocalDateTime> tableColumnMatchDate;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnMatchResult;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnPlayersBlue;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnPlayersRed;
    @FXML
    private Button compareSelectedMatchesButton;

    public TeamMatchesController(SpringFXMLLoader springFXMLLoader, TeamCompareController teamCompareController, TeamService teamService) {
        this.springFXMLLoader = springFXMLLoader;
        this.teamCompareController = teamCompareController;
        this.teamService = teamService;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    private void initialize() {
        tableViewMatches.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        tableColumnMatchDate.setCellValueFactory(new PropertyValueFactory<>("formattedDateTime"));
        tableColumnMatchResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        tableColumnPlayersRed.setCellValueFactory(new PropertyValueFactory<>("teamRedPlayers"));
        tableColumnPlayersBlue.setCellValueFactory(new PropertyValueFactory<>("teamBluePlayers"));

        tableColumnMatchResult.setStyle("-fx-alignment: CENTER;");
        tableColumnMatchDate.setStyle("-fx-alignment: CENTER;");
        tableColumnPlayersBlue.setStyle("-fx-alignment: CENTER;");
        tableColumnPlayersRed.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * fills table with all matches of team1 vs team2
     *
     * @param team1 team to be compared
     * @param team2 team to be compared
     */
    void loadMatches(TeamDTO team1, TeamDTO team2) {
        this.team1 = team1;
        this.team2 = team2;

        try {
            teamCompareDTO = teamService.readTeamMatches(team1, team2);
            ObservableList<MatchDTO> matches = FXCollections.observableArrayList(teamCompareDTO.getMatchDTOList());

            tableViewMatches.setItems(matches);
            tableViewMatches.getSelectionModel().selectAll();

        } catch (TeamServiceException e) {
            LOG.error("Failed to read team stats", e);
            AlertHelper.showErrorMessage("Fehler beim Anzeigen der Team Matches.");
        }
    }

    @FXML
    private void onCompareMatchesButtonClicked() {
        List<MatchDTO> selectedMatches = tableViewMatches.getSelectionModel().getSelectedItems();

        if (!selectedMatches.isEmpty()) {
            try {

                Stage teamCompareStage = new Stage();
                // setup application
                teamCompareStage.setTitle("Compare Teams");
                teamCompareStage.setWidth(1024);
                teamCompareStage.setHeight(768);
                teamCompareStage.centerOnScreen();
                teamCompareStage.setOnCloseRequest(event -> LOG.debug("Compare Teams window closed"));
                teamCompareStage.setScene(new Scene(springFXMLLoader.load("/fxml/teamComparePage.fxml", Parent.class)));

                teamCompareDTO.setMatchDTOList(selectedMatches);
                teamCompareController.setTeamCompareData(teamCompareDTO, team1, team2);

                ((Stage) compareSelectedMatchesButton.getScene().getWindow()).close();
                teamCompareStage.toFront();
                teamCompareStage.show();
            } catch (IOException e) {
                LOG.error("Loading Compare Team fxml failed", e);
                AlertHelper.showErrorMessage("Fenster zum Vergleichen der Teams konnte nicht geöffnet werden.");
            }
        } else {
            AlertHelper.alert(Alert.AlertType.INFORMATION, "Info", null, "Es wurde kein Match ausgewählt.");
        }
    }
}

