package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;

@Component
public class TeamController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private ExecutorService executorService;
    private TeamService teamService;
    private PlayerService playerService;

    @FXML
    private TableView<TeamDTO> tableViewTeams;
    @FXML
    private TableColumn<TeamDTO, String> tableColumnTeamName;

    public TeamController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService, TeamService teamService, PlayerService playerService) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
        this.teamService = teamService;
        this.playerService = playerService;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {
        tableColumnTeamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableViewTeams.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableColumnTeamName.setStyle("-fx-alignment: CENTER;");

        updateTeamTable();
    }

    public void onNewTeamButtonClicked(ActionEvent actionEvent) {
        Stage newTeamStage = new Stage();
        // setup application
        newTeamStage.setTitle("New Team");
        newTeamStage.setWidth(1024);
        newTeamStage.setHeight(768);
        newTeamStage.centerOnScreen();
        newTeamStage.setOnCloseRequest(event -> {
            LOG.debug("New Team window closed");
        });


        try {
            newTeamStage.setScene(new Scene(springFXMLLoader.load("/fxml/newTeam.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading New Team fxml failed", e);
        }
        newTeamStage.toFront();
        newTeamStage.showAndWait();
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
            LOG.debug("");
        } catch (TeamServiceException e) {
            LOG.error("Caught TeamServiceException {} ", e.getMessage());
            AlertHelper.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Deletes a selected Team
     * @param actionEvent Actionevent from the button
     */
    public void onDeleteTeamButtonClicked(ActionEvent actionEvent) {
        LOG.info("Delete Team Button clicked");
        LOG.trace("called - onDeleteTeamButtonClicked");
        if (tableViewTeams.getSelectionModel().getSelectedItem() != null) {
            TeamDTO selectedTeam = tableViewTeams.getSelectionModel().getSelectedItem();
            try {
                teamService.deleteTeam(selectedTeam);
                //TODO: updateTeamTable();
            } catch (TeamValidationException e) {
                LOG.error("caught TeamValidationException", e);
                AlertHelper.showErrorMessage(e.getMessage());
            } catch (TeamServiceException e) {
                LOG.error("caught TeamServiceException", e);
                AlertHelper.showErrorMessage(e.getMessage());
            }
        } else {
            AlertHelper.showErrorMessage("No team selected");
        }
    }
}
