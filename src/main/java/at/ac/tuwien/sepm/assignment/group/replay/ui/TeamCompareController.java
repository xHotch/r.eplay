package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Daniel Klampfl
 */
@Controller
public class TeamCompareController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TeamDTO teamDTO1;
    private TeamDTO teamDTO2;
    private TeamCompareDTO teamCompareDTO;

    private String team1Color = "#f04555";
    private String team2Color = "#246dfa";

    @FXML
    private BarChart<String,Double> teamBarChart;
    @FXML
    private NumberAxis teamNumberAxis;
    @FXML
    private ChoiceBox<String> matchValueChoiceBox;

    @FXML
    private TableView<PlayerDTO> team1Table;
    @FXML
    private TableColumn<PlayerDTO, String> playerName1Column;
    @FXML
    private TableView<PlayerDTO> team2Table;
    @FXML
    private TableColumn<PlayerDTO, String> playerName2Column;

    @FXML
    private void initialize()
    {
        matchValueChoiceBox.getItems().addAll("Tore","Paraden","Vorlagen","Schüsse","Punkte","⌀ Geschwindigkeit");
        matchValueChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
            (obs, oldValue, newValue) -> showMatchValue(newValue.intValue())
        );
        teamBarChart.setAnimated(false);
        teamBarChart.setLegendVisible(false);
        teamNumberAxis.setAutoRanging(true);
        setupPlayerTable();
    }

    void setTeamCompareData(TeamCompareDTO teamCompareDTO, TeamDTO teamDTO1, TeamDTO teamDTO2){
        //filter matchStatsList to match MatchDTOList
        teamCompareDTO.setMatchStatsDTOList(teamCompareDTO.getMatchStatsDTOList().entrySet().stream()
            .filter(e -> teamCompareDTO.getMatchDTOList().stream().anyMatch(m -> m.getId() == e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        this.teamCompareDTO = teamCompareDTO;
        this.teamDTO1 = teamDTO1;
        this.teamDTO2 = teamDTO2;
        updatePlayerTable();
    }

    /**
     * Helper Method to setup up the player team Table Columns
     */
    private void setupPlayerTable() {
        playerName2Column.setCellValueFactory(new PropertyValueFactory<>("name"));
        playerName1Column.setCellValueFactory(new PropertyValueFactory<>("name"));
        playerName2Column.setStyle("-fx-alignment: CENTER;");
        playerName1Column.setStyle("-fx-alignment: CENTER;");
        playerName1Column.setStyle("-fx-text-fill: " + team1Color + ";");
        playerName2Column.setStyle("-fx-text-fill: " + team2Color + ";");
    }

    private void updatePlayerTable() {
        ObservableList<PlayerDTO> team1Players = FXCollections.observableArrayList(teamDTO1.getPlayers());
        team1Table.setItems(team1Players);
        playerName1Column.setText(teamDTO1.getName());
        ObservableList<PlayerDTO> team2Players = FXCollections.observableArrayList(teamDTO2.getPlayers());
        team2Table.setItems(team2Players);
        playerName2Column.setText(teamDTO2.getName());
        matchValueChoiceBox.getSelectionModel().select(0);
    }

    private void showMatchValue(int itemIndex) {
        LOG.info("Show match Value: {}", matchValueChoiceBox.getItems().get(itemIndex));
        teamBarChart.getData().clear();

        XYChart.Series<String,Double> team1 = new XYChart.Series<>();
        XYChart.Series<String,Double> team2 = new XYChart.Series<>();

        team1.setName(teamDTO1.getName());
        team2.setName(teamDTO2.getName());

        int i = 1;
        for (List<MatchStatsDTO> matchStatsDTOList : teamCompareDTO.getMatchStatsDTOList().values()) {
            for (MatchStatsDTO matchStatsDTO : matchStatsDTOList) {
                double value;
                switch (itemIndex) {
                    case 0:
                        value = matchStatsDTO.getGoals();
                        break;
                    case 1:
                        value = matchStatsDTO.getAssists();
                        break;
                    case 2:
                        value = matchStatsDTO.getShots();
                        break;
                    case 3:
                        value = matchStatsDTO.getSaves();
                        break;
                    case 4:
                        value = matchStatsDTO.getScore();
                        break;
                    case 5:
                        value = matchStatsDTO.getAverageSpeed();
                        break;
                    default:
                        value = 0;
                        break;
                }
                if(matchStatsDTO.getTeamId() == teamDTO1.getId()) team1.getData().add(new XYChart.Data<>("Match " + i,value));
                else team2.getData().add(new XYChart.Data<>("Match " + i,value));
            }
            i++;
        }
        teamBarChart.getData().add(team1);
        teamBarChart.getData().add(team2);

        //set color for bars depending on team
        for (XYChart.Data data : team1.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + team1Color + ";");
        }
        for (XYChart.Data data : team2.getData()) {
            data.getNode().setStyle("-fx-bar-fill:  " + team2Color + ";");
        }

    }
}
