package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Component
public class TeamController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private TeamService teamService;
    private TeamMatchesController teamMatchesController;

    @FXML
    private TableView<TeamDTO> tableViewTeams;
    @FXML
    private TableColumn<TeamDTO, String> tableColumnTeamName;
    @FXML
    private Text txtTeamName;
    @FXML
    private Text txtTeamSize;
    @FXML
    private Text txtPlayer1;
    @FXML
    private Text txtPlayer2;
    @FXML
    private Text txtPlayer3;

    public TeamController(SpringFXMLLoader springFXMLLoader, TeamService teamService, TeamMatchesController teamMatchesController) {
        this.springFXMLLoader = springFXMLLoader;
        this.teamService = teamService;
        this.teamMatchesController = teamMatchesController;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    private void initialize() {
        tableColumnTeamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableViewTeams.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableViewTeams.getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> showTeamDetails(newValue));
        tableColumnTeamName.setStyle("-fx-alignment: CENTER;");

        updateTeamTable();
    }

    @FXML
    private void onNewTeamButtonClicked() {
        LOG.info("new team button clicked");
        Stage newTeamStage = new Stage();
        // setup application
        newTeamStage.setTitle("New Team");
        newTeamStage.setWidth(1024);
        newTeamStage.setHeight(768);
        newTeamStage.centerOnScreen();
        newTeamStage.setOnCloseRequest(event -> LOG.debug("New Team window closed"));


        try {
            newTeamStage.setScene(new Scene(springFXMLLoader.load("/fxml/newTeam.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading New Team fxml failed", e);
            AlertHelper.showErrorMessage("Fenster zum Team anlegen konnte nicht geöffnet werden.");
        }
        newTeamStage.toFront();
        newTeamStage.showAndWait();
    }

    @FXML
    private void onTeamCompareButtonClicked() {
        LOG.info("team compare button clicked");
        if (!tableViewTeams.getSelectionModel().getSelectedItems().isEmpty() && tableViewTeams.getSelectionModel().getSelectedItems().size() == 2) {
            List<TeamDTO> selectedTeams = tableViewTeams.getSelectionModel().getSelectedItems();

            if (selectedTeams.get(0).getTeamSize() != selectedTeams.get(1).getTeamSize()) {
                AlertHelper.alert(Alert.AlertType.INFORMATION, "Info", null, "Die ausgewählten Teams müssen gleich groß sein.");
            } else {
                Stage newTeamStage = new Stage();
                // setup application
                newTeamStage.setTitle("Matches auswählen");
                newTeamStage.setWidth(1024);
                newTeamStage.setHeight(768);
                newTeamStage.centerOnScreen();
                newTeamStage.setOnCloseRequest(event -> LOG.debug("Select team matches window closed"));
                try {
                    newTeamStage.setScene(new Scene(springFXMLLoader.load("/fxml/teamMatchesPage.fxml", Parent.class)));
                } catch (IOException e) {
                    LOG.error("Loading teamMatches fxml failed", e);
                    AlertHelper.showErrorMessage("Fenster für die Match Auswahl konnte nicht geöffnet werden.");
                }
                teamMatchesController.loadMatches(selectedTeams.get(0), selectedTeams.get(1));
                newTeamStage.toFront();
                newTeamStage.showAndWait();
            }
        } else {
            AlertHelper.alert(Alert.AlertType.INFORMATION, "Info", null, "Es müssen genau 2 Teams zum Vergleichen ausgewählt werden.");
        }
    }

    /**
     * Loads the teams into the teams table
     * Calls the showErrorMessage if an Exception occurs
     */
    void updateTeamTable() {
        LOG.trace("called - updateTeamTable");
        try {
            ObservableList<TeamDTO> observableTeams = FXCollections.observableArrayList(teamService.readTeams());

            tableViewTeams.setItems(observableTeams);
        } catch (TeamServiceException e) {
            LOG.error("Caught TeamServiceException", e);
            AlertHelper.showErrorMessage("Fehler beim Laden der Teams");
        }
    }

    /**
     * Deletes selected teams
     */
    @FXML
    private void onDeleteTeamsButtonClicked() {
        LOG.info("Delete Team Button clicked");
        LOG.trace("called - onDeleteTeamsButtonClicked");
        if (tableViewTeams.getSelectionModel().getSelectedItems() != null) {
            List<TeamDTO> selectedTeams = tableViewTeams.getSelectionModel().getSelectedItems();
            try {
                for (TeamDTO team : selectedTeams) {
                    teamService.deleteTeam(team);
                }
                updateTeamTable();
                deleteTeamTexts();
            } catch (TeamValidationException e) {
                LOG.error("caught TeamValidationException", e);
                AlertHelper.showErrorMessage("Fehler beim Löschen des Teams: Ungültiges Team: " + e.getMessage());
            } catch (TeamServiceException e) {
                LOG.error("caught TeamServiceException", e);
                AlertHelper.showErrorMessage("Fehler beim Löschen des Teams: Team konnte nicht gelöscht werden");
            }
        } else {
            AlertHelper.showErrorMessage("Kein Team ausgewählt");
        }
    }

    /**
     * shows the team details for a selected team on the right side of the table
     *
     * @param selectedTeam team that is currently selected
     */
    private void showTeamDetails(TeamDTO selectedTeam) {
        LOG.info("Show Team Details Button clicked");
        LOG.trace("called - onShowTeamDetailsButtonClicked");
        if (selectedTeam != null) {
            updateTeamDetails(selectedTeam);
        }
    }

    /**
     * updates team details text depending on the selected team
     *
     * @param selectedTeam team to be updated
     */
    private void updateTeamDetails(TeamDTO selectedTeam) {
        deleteTeamTexts();

        txtTeamName.setText(selectedTeam.getName());
        txtTeamSize.setText("" + selectedTeam.getTeamSize());
        List<PlayerDTO> players = selectedTeam.getPlayers();
        if (selectedTeam.getTeamSize() == 3) {
            txtPlayer1.setText(players.get(0).getName());
            txtPlayer2.setText(players.get(1).getName());
            txtPlayer3.setText(players.get(2).getName());

        } else if (selectedTeam.getTeamSize() == 2) {
            txtPlayer1.setText(players.get(0).getName());
            txtPlayer2.setText(players.get(1).getName());
        } else {
            txtPlayer1.setText(players.get(0).getName());
        }

    }

    private void deleteTeamTexts() {
        txtTeamName.setText("");
        txtTeamSize.setText("");
        txtPlayer1.setText("");
        txtPlayer2.setText("");
        txtPlayer3.setText("");
    }
}
