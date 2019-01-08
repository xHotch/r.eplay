package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamValidationException;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.event.ActionEvent;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
public class NewTeamController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private ExecutorService executorService;
    private PlayerService playerService;
    private TeamService teamService;
    private TeamController teamController;

    @FXML
    private TableView<PlayerDTO> tableViewPlayers;
    @FXML
    private TableColumn<PlayerDTO, String> tableColumnPlayerName;
    @FXML
    private TextField textFieldName;
    @FXML
    private ComboBox<Integer> comboBoxTeamSize;


    @FXML
    private void onSaveTeamButtonClicked(ActionEvent event) {
        LOG.trace("called - onSaveTeamButtonClicked");
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setName(textFieldName.getText());
        teamDTO.setTeamSize(comboBoxTeamSize.getSelectionModel().getSelectedItem());
        teamDTO.setPlayers(tableViewPlayers.getSelectionModel().getSelectedItems());
        try {
            teamService.createTeam(teamDTO);
            LOG.debug("after create Team");
            teamController.updateTeamTable();
            LOG.debug("after update TeamTable");
            ((Stage)(((Node)event.getSource()).getScene().getWindow())).close();
        } catch (TeamValidationException e) {
            AlertHelper.showErrorMessage(e.getMessage());
        } catch (TeamServiceException e) {
            LOG.error(e.getMessage(), e);
            AlertHelper.showErrorMessage(e.getMessage());
        }


    }


    public NewTeamController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService, PlayerService playerService, TeamService teamService, TeamController teamController) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
        this.playerService = playerService;
        this.teamService = teamService;
        this.teamController = teamController;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    private void initialize() {
        tableColumnPlayerName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableViewPlayers.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableColumnPlayerName.setStyle("-fx-alignment: CENTER;");

        try {
            ObservableList<PlayerDTO> observablePlayers = FXCollections.observableArrayList(playerService.getPlayers());

            tableViewPlayers.setItems(observablePlayers);
        } catch (PlayerServiceException e) {
            LOG.error("Caught PlayerServiceException {} ", e.getMessage());
            AlertHelper.showErrorMessage(e.getMessage());
        }
        comboBoxTeamSize.getItems().add(1);
        comboBoxTeamSize.getItems().add(2);
        comboBoxTeamSize.getItems().add(3);
        comboBoxTeamSize.getSelectionModel().selectFirst();
    }
}
