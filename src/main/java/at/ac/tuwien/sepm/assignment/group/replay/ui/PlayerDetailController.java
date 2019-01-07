package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.AvgStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchType;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    @FXML
    void initialize() {
        typChoiceBox.getItems().addAll(MatchType.RANKED1V1, MatchType.RANKED2V2, MatchType.RANKED3V3);
    }

    void loadPlayer(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;

        playerLabel.setText(playerDTO.getName());
        winsLabel.setText("");
        lossesLabel.setText("");

        ChangeListener<MatchType> changeListener = (observable, oldType, newType) -> {
            showCurrentTypStatistics(newType);
        };
        typChoiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }

    private void showCurrentTypStatistics(MatchType newType) {
        /*AvgStatsDTO avgStatsDTO = playerService.getAvgStatistics(playerDTO, newType);
        List<AvgStatsDTO> list = new ArrayList<>();
        list.add(avgStatsDTO);
        ObservableList<AvgStatsDTO> items = FXCollections.observableArrayList(list);
        avgTableView.setItems(items);
        winsLabel.setText(playerService.getWins(playerDTO, newType));
        lossesLabel.setText(playerService.getLosses(playerDTO, newType));*/
    }

}
