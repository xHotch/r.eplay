package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Gabriel Aichinger
 */
@Component
public class MatchPlayerStatisticsController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private JsonParseService jsonParseService;

    private MatchDTO matchDTO;

    @FXML
    private PieChart pcAirGroundTime;
    @FXML
    private PieChart pcTimeInSide;
    @FXML
    private ChoiceBox<String> cbMatchPlayer;
    @FXML
    private BarChart<String, Double> bcAvgDistanceToBall;
    @FXML
    private NumberAxis bcYAxis;
    @FXML
    private CategoryAxis bcXAxis;
    @FXML
    private ImageView heatmapView;

    public MatchPlayerStatisticsController(JsonParseService jsonParseService) {
        this.jsonParseService = jsonParseService;
    }

    /**
     * Adds match player names to the choice box. Creates bar chart for average distance to ball for all players.
     *
     * @param matchDTO MatchDTO of which the players should be selectable
     */
    void loadMatchPlayerStatistics(MatchDTO matchDTO) {
        LOG.trace("called loadMatchPlayerStatistics");

        this.matchDTO = matchDTO;

        //create series that can be added to the bar chart
        XYChart.Series<String, Double> seriesBlue = new XYChart.Series<>();
        XYChart.Series<String, Double> seriesRed = new XYChart.Series<>();

        //set players in choice box and add data to series
        ObservableList<String> matchPlayers = FXCollections.observableArrayList();
        List<String> blueTeam = new LinkedList<>();
        List<String> redTeam = new LinkedList<>();

        for (MatchPlayerDTO player : matchDTO.getPlayerData()) {
            if (player.getTeam() == TeamSide.BLUE) {
                blueTeam.add(player.getPlayerDTO().getName());
                seriesBlue.getData().add(new XYChart.Data<>(player.getPlayerDTO().getName(), player.getAverageDistanceToBall()));
            } else {
                redTeam.add(player.getPlayerDTO().getName());
                seriesRed.getData().add(new XYChart.Data<>(player.getPlayerDTO().getName(), player.getAverageDistanceToBall()));
            }
        }
        matchPlayers.addAll(blueTeam);
        matchPlayers.addAll(redTeam);

        //add series to bar chart
        bcAvgDistanceToBall.getData().add(seriesBlue);
        bcAvgDistanceToBall.getData().add(seriesRed);

        //set bar chart properties
        bcAvgDistanceToBall.setBarGap(-17);
        bcXAxis.setTickLabelRotation(90);
        bcYAxis.setAutoRanging(true);
        bcAvgDistanceToBall.setLegendVisible(false);

        //set color for bars depending on team
        for (XYChart.Data data : seriesBlue.getData()) {
            data.getNode().setStyle("-fx-bar-fill: lightskyblue;");
        }
        for (XYChart.Data data : seriesRed.getData()) {
            data.getNode().setStyle("-fx-bar-fill: lightcoral;");
        }

        //set choice box values
        cbMatchPlayer.setItems(matchPlayers);

        //add change listener, so the diagrams are updated when another player is selected
        ChangeListener<String> changeListener = (observable, oldName, newName) -> {
            if (newName != null) {
                showCurrentPlayerStatistics(newName);
            }
        };
        cbMatchPlayer.getSelectionModel().selectedItemProperty().addListener(changeListener);
        cbMatchPlayer.getSelectionModel().select(0);
    }


    /**
     * updates diagrams depending on which player was selected
     *
     * @param playerName Name of the selected player
     */
    private void showCurrentPlayerStatistics(String playerName) {

        //clear pie charts
        pcAirGroundTime.getData().clear();
        pcTimeInSide.getData().clear();

        //get selected Player object
        MatchPlayerDTO selectedPlayer = new MatchPlayerDTO();

        for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
            if (matchPlayerDTO.getPlayerDTO().getName().equals(playerName)) {
                selectedPlayer = matchPlayerDTO;
            }
        }


        //get player air/ground time info and add it to the air vs ground time chart
        PieChart.Data airTimeSlice = new PieChart.Data("in der Luft", selectedPlayer.getAirTime());
        PieChart.Data groundTimeSlice = new PieChart.Data("am Boden", selectedPlayer.getGroundTime());
        pcAirGroundTime.getData().add(airTimeSlice);
        pcAirGroundTime.getData().add(groundTimeSlice);

        //get player air/ground time info and add it to the air vs ground time chart
        PieChart.Data homeSideTimeSlice = new PieChart.Data("eigene Hälfte", selectedPlayer.getHomeSideTime());
        PieChart.Data enemySideTimeSlice = new PieChart.Data("gegnerische Hälfte", selectedPlayer.getEnemySideTime());
        pcTimeInSide.getData().add(homeSideTimeSlice);
        pcTimeInSide.getData().add(enemySideTimeSlice);

        pcAirGroundTime.setStartAngle(90);
        pcTimeInSide.setStartAngle(90);

        //set colors
        String lightskyblue = "-fx-pie-color: lightskyblue;";
        //set air and ground time colors
        airTimeSlice.getNode().setStyle(lightskyblue);
        groundTimeSlice.getNode().setStyle("-fx-pie-color: tan;");
        //set home side color depending on the team of the player
        if (selectedPlayer.getTeam() == TeamSide.BLUE) {
            homeSideTimeSlice.getNode().setStyle(lightskyblue);
            enemySideTimeSlice.getNode().setStyle("-fx-pie-color: lightcoral;");
        } else {
            homeSideTimeSlice.getNode().setStyle("-fx-pie-color: lightcoral;");
            enemySideTimeSlice.getNode().setStyle(lightskyblue);
        }

        heatmapView.setImage(SwingFXUtils.toFXImage(selectedPlayer.getHeatmapImage(), null));
        heatmapView.setRotate(270);
        heatmapView.setScaleX(1.5);
        heatmapView.setScaleY(1.5);
    }
}
