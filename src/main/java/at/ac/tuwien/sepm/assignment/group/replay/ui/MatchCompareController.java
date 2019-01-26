package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.TeamServiceException;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MatchCompareController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MatchService matchService;
    private TeamService teamService;

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
    private TableView<PlayerTeamsDTO> match1TableRed;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> playerName1ColumnRed;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> team1ColumnRed;
    @FXML
    private TableView<PlayerTeamsDTO> match2TableRed;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> playerName2ColumnRed;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> team2ColumnRed;

    @FXML
    private TableView<PlayerTeamsDTO> match1TableBlue;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> playerName1ColumnBlue;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> team1ColumnBlue;
    @FXML
    private TableView<PlayerTeamsDTO> match2TableBlue;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> playerName2ColumnBlue;
    @FXML
    private TableColumn<PlayerTeamsDTO, String> team2ColumnBlue;

    public MatchCompareController(MatchService matchService, TeamService teamService) {
        this.matchService = matchService;
        this.teamService = teamService;
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
        setUpTables(matchDTO1, matchDTO2);
    }

    private void setUpTables(MatchDTO matchDTO1, MatchDTO matchDTO2) {
        playerName1ColumnRed.setStyle("-fx-text-fill: " + team1Color + ";");
        playerName2ColumnRed.setStyle("-fx-text-fill: " + team1Color + ";");
        playerName1ColumnBlue.setStyle("-fx-text-fill: " + team2Color + ";");
        playerName2ColumnBlue.setStyle("-fx-text-fill: " + team2Color + ";");
        team1ColumnRed.setStyle("-fx-text-fill: " + team1Color + ";");
        team2ColumnRed.setStyle("-fx-text-fill: " + team1Color + ";");
        team1ColumnBlue.setStyle("-fx-text-fill: " + team2Color + ";");
        team2ColumnBlue.setStyle("-fx-text-fill: " + team2Color + ";");

        playerName1ColumnRed.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        playerName2ColumnRed.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        team1ColumnRed.setCellValueFactory(new PropertyValueFactory<>("teamNames"));
        team2ColumnRed.setCellValueFactory(new PropertyValueFactory<>("teamNames"));
        playerName1ColumnBlue.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        playerName2ColumnBlue.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        team1ColumnBlue.setCellValueFactory(new PropertyValueFactory<>("teamNames"));
        team2ColumnBlue.setCellValueFactory(new PropertyValueFactory<>("teamNames"));

        List<PlayerTeamsDTO> match1ListRed = new ArrayList<>();
        List<PlayerTeamsDTO> match2ListRed = new ArrayList<>();
        List<PlayerTeamsDTO> match1ListBlue = new ArrayList<>();
        List<PlayerTeamsDTO> match2ListBlue = new ArrayList<>();
        try {
            setPlayerLists(matchDTO1, match1ListRed, match1ListBlue);
            setPlayerLists(matchDTO2, match2ListRed, match2ListBlue);

            ObservableList<PlayerTeamsDTO> list1Red = FXCollections.observableArrayList(match1ListRed);
            ObservableList<PlayerTeamsDTO> list2Red = FXCollections.observableArrayList(match2ListRed);
            ObservableList<PlayerTeamsDTO> list1Blue = FXCollections.observableArrayList(match1ListBlue);
            ObservableList<PlayerTeamsDTO> list2Blue = FXCollections.observableArrayList(match2ListBlue);
            match1TableRed.setItems(list1Red);
            match2TableRed.setItems(list2Red);
            match1TableBlue.setItems(list1Blue);
            match2TableBlue.setItems(list2Blue);
        } catch (TeamServiceException e) {
            LOG.error("Failed to read player teams", e);
            AlertHelper.showErrorMessage("Fehler beim Anzeigen der Spieler Teams.");
        }
    }

    private void setPlayerLists(MatchDTO matchDTO1, List<PlayerTeamsDTO> match1ListRed, List<PlayerTeamsDTO> match1ListBlue) throws TeamServiceException {
        for(MatchPlayerDTO matchPlayerDTO : matchDTO1.getPlayerData()) {
            PlayerTeamsDTO playerTeamsDTO = teamService.readPlayerTeams(matchPlayerDTO.getPlayerDTO());
            if (matchPlayerDTO.getTeam() == TeamSide.RED) {
                match1ListRed.add(playerTeamsDTO);
            } else {
                match1ListBlue.add(playerTeamsDTO);
            }
        }
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


