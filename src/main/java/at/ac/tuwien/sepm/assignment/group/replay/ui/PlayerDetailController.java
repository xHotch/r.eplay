package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.AvgStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.PlayerServiceException;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlayerDetailController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private PlayerService playerService;
    private PlayerDTO playerDTO;

    @FXML
    private Label playerLabel;

    @FXML
    private ChoiceBox<MatchType> typChoiceBox;

    @FXML
    private Label winsLabel;

    @FXML
    private Label lossesLabel;

    @FXML
    private TableView<AvgStatsDTO> avgTableView;

    @FXML
    private TableColumn<AvgStatsDTO, Double> goalsTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> assistsTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> savesTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> shotsTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> scoreTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> speedTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> boostpadTableColumn;

    @FXML
    private TableColumn<AvgStatsDTO, Double> boostTableColumn;

    public PlayerDetailController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @FXML
    private void initialize() {
        typChoiceBox.getItems().addAll(MatchType.RANKED1V1, MatchType.RANKED2V2, MatchType.RANKED3V3);
        typChoiceBox.getSelectionModel().selectFirst();
        assistsTableColumn.setCellValueFactory(new PropertyValueFactory<>("assists"));
        boostpadTableColumn.setCellValueFactory(new PropertyValueFactory<>("boostpads"));
        boostTableColumn.setCellValueFactory(new PropertyValueFactory<>("boost"));
        goalsTableColumn.setCellValueFactory(new PropertyValueFactory<>("goals"));
        savesTableColumn.setCellValueFactory(new PropertyValueFactory<>("saves"));
        scoreTableColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        shotsTableColumn.setCellValueFactory(new PropertyValueFactory<>("shots"));
        speedTableColumn.setCellValueFactory(new PropertyValueFactory<>("speed"));
        formatCellText(assistsTableColumn);
        formatCellText(boostpadTableColumn);
        formatCellText(boostTableColumn);
        formatCellText(goalsTableColumn);
        formatCellText(savesTableColumn);
        formatCellText(scoreTableColumn);
        formatCellText(shotsTableColumn);
        formatCellText(speedTableColumn);
    }

    void loadPlayer(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;

        playerLabel.setText(playerDTO.getName());
        winsLabel.setText("");
        lossesLabel.setText("");

        showCurrentTypStatistics(typChoiceBox.getSelectionModel().getSelectedItem());
        ChangeListener<MatchType> changeListener = (observable, oldType, newType) -> {
            showCurrentTypStatistics(newType);
        };
        typChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }

    private void showCurrentTypStatistics(MatchType newType) {
        try {
            AvgStatsDTO avgStatsDTO = playerService.getAvgStats(playerDTO, newType);
            List<AvgStatsDTO> list = new ArrayList<>();
            list.add(avgStatsDTO);
            ObservableList<AvgStatsDTO> items = FXCollections.observableArrayList(list);
            avgTableView.setItems(items);
            winsLabel.setText("" + avgStatsDTO.getWins());
            lossesLabel.setText("" + avgStatsDTO.getLosses());
        } catch (PlayerServiceException e) {
            LOG.error("Caught PlayerServiceException {} ", e.getMessage());
            AlertHelper.showErrorMessage("Fehler beim Laden der Spieler Details");
        }

    }

    private void formatCellText(TableColumn<AvgStatsDTO, Double> column){
        column.setCellFactory(tc -> new TableCell<>() {

            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", value));
                }
            }
        });
    }

}
