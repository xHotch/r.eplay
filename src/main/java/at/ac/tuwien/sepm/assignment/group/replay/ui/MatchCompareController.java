package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;

@Controller
public class MatchCompareController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MatchService matchService;

    private String team1Color = "#f04555";
    private String team2Color = "#246dfa";

    private MatchStatsDTO match1blue;
    private MatchStatsDTO match1red;
    private MatchStatsDTO match2blue;
    private MatchStatsDTO match2red;

    @FXML
    private BarChart<String,Double> matchBarChart;
    @FXML
    private NumberAxis matchNumberAxis;
    @FXML
    private ChoiceBox<String> matchValueChoiceBox;

    @FXML
    private TableView<?> match1Table;
    @FXML
    private TableColumn<?, ?> playerName1Column;
    @FXML
    private TableColumn<?, ?> team1Column;
    @FXML
    private TableView<?> match2Table;
    @FXML
    private TableColumn<?, ?> playerName2Column;
    @FXML
    private TableColumn<?, ?> team2Column;

    public MatchCompareController(MatchService matchService) {
        this.matchService = matchService;
    }

    @FXML
    private void initialize() {
        matchValueChoiceBox.getItems().addAll("Tore", "Paraden", "Vorlagen", "Schüsse", "Punkte", "⌀ Geschwindigkeit");
        matchValueChoiceBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) -> showMatchValue(newValue.intValue()));
        matchBarChart.setAnimated(false);
        matchBarChart.setLegendVisible(false);
        matchNumberAxis.setAutoRanging(true);
    }

    void setUp(MatchDTO matchDTO1, MatchDTO matchDTO2) {
        match1red = matchService.calcTeamStats(matchDTO1, TeamSide.RED);
        match1blue = matchService.calcTeamStats(matchDTO1, TeamSide.BLUE);
        match2red = matchService.calcTeamStats(matchDTO2, TeamSide.RED);
        match2blue = matchService.calcTeamStats(matchDTO2, TeamSide.BLUE);
        matchValueChoiceBox.getSelectionModel().select(0);
    }

    private void showMatchValue(int itemIndex) {
        LOG.info("Show match Value: {}", matchValueChoiceBox.getSelectionModel().getSelectedItem());
        matchBarChart.getData().clear();

        XYChart.Series<String,Double> team1 = new XYChart.Series<>();
        XYChart.Series<String,Double> team2 = new XYChart.Series<>();

        team1.setName("Blau");
        team2.setName("Rot");

        double valueMatch1Blue = 0;
        double valueMatch1Red = 0;
        double valueMatch2Blue = 0;
        double valueMatch2Red = 0;
        switch (itemIndex) {
            case 0:
                valueMatch1Blue = match1blue.getGoals();
                valueMatch1Red = match1red.getGoals();
                valueMatch2Blue = match2blue.getGoals();
                valueMatch2Red = match2red.getGoals();
                break;
            case 1:
                valueMatch1Blue = match1blue.getAssists();
                valueMatch1Red = match1red.getAssists();
                valueMatch2Blue = match2blue.getAssists();
                valueMatch2Red = match2red.getAssists();
                break;
            case 2:
                valueMatch1Blue = match1blue.getShots();
                valueMatch1Red = match1red.getShots();
                valueMatch2Blue = match2blue.getShots();
                valueMatch2Red = match2red.getShots();
                break;
            case 3:
                valueMatch1Blue = match1blue.getSaves();
                valueMatch1Red = match1red.getSaves();
                valueMatch2Blue = match2blue.getSaves();
                valueMatch2Red = match2red.getSaves();
                break;
            case 4:
                valueMatch1Blue = match1blue.getScore();
                valueMatch1Red = match1red.getScore();
                valueMatch2Blue = match2blue.getScore();
                valueMatch2Red = match2red.getScore();
                break;
            case 5:
                valueMatch1Blue = match1blue.getAverageSpeed();
                valueMatch1Red = match1red.getAverageSpeed();
                valueMatch2Blue = match2blue.getAverageSpeed();
                valueMatch2Red = match2red.getAverageSpeed();
                break;
            default:
                break;
        }
        team1.getData().add(new XYChart.Data<>("Match 1" ,valueMatch1Blue));
        team1.getData().add(new XYChart.Data<>("Match 2", valueMatch2Blue));
        team2.getData().add(new XYChart.Data<>("Match 1" ,valueMatch1Red));
        team2.getData().add(new XYChart.Data<>("Match 2", valueMatch2Red));

        matchBarChart.getData().add(team1);
        matchBarChart.getData().add(team2);

        //set color for bars depending on team
        for (XYChart.Data data : team1.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + team1Color + ";");
        }
        for (XYChart.Data data : team2.getData()) {
            data.getNode().setStyle("-fx-bar-fill:  " + team2Color + ";");
        }

    }
}


