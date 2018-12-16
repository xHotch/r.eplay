package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * @author Gabriel Aichinger
 */
@Component
public class MatchPlayerStatisticsController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private MatchDTO matchDTO;

    @FXML
    private PieChart pcAirGroundTime;
    @FXML
    private PieChart pcTimeInSide;
    @FXML
    private ChoiceBox<String> cbMatchPlayer;


    void loadMatchPlayerStatistics(MatchDTO matchDTO) {
        LOG.trace("called loadMatchPlayerStatistics");

        this.matchDTO = matchDTO;

        //set players in choice box
        ObservableList<String> matchPlayers = FXCollections.observableArrayList();
        for (MatchPlayerDTO player : matchDTO.getPlayerData()) {
            matchPlayers.add(player.getPlayerDTO().getName());
        }
        cbMatchPlayer.setItems(matchPlayers);

        ChangeListener<String> changeListener = (observable, oldName, newName) -> {
            if (newName != null) {
                showCurrentPlayerStatistics(newName);
            }
        };

        cbMatchPlayer.getSelectionModel().selectedItemProperty().addListener(changeListener);
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
        pcAirGroundTime.setLegendVisible(false);
        pcTimeInSide.setLegendVisible(false);

        //set colors
        String lightskyblue = "-fx-pie-color: lightskyblue;";

        airTimeSlice.getNode().setStyle(lightskyblue);
        groundTimeSlice.getNode().setStyle("-fx-pie-color: tan;");
        if (selectedPlayer.getTeam() == TeamSide.BLUE) {
            homeSideTimeSlice.getNode().setStyle(lightskyblue);
            enemySideTimeSlice.getNode().setStyle("-fx-pie-color: lightcoral;");
        } else {
            homeSideTimeSlice.getNode().setStyle("-fx-pie-color: lightcoral;");
            enemySideTimeSlice.getNode().setStyle(lightskyblue);
        }

        //TODO: add heatmap


    }
}
