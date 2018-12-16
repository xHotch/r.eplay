package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Ball Statistics Tab Page Controller
 *
 * @author Gabriel Aichinger
 */
@Component
public class BallStatisticsController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private PieChart pieChartTeamSide;
    @FXML
    private PieChart pieChartPossession;

    void loadBallStatistics(MatchDTO matchDTO) {

        //get ball info and add it to the team side chart
        PieChart.Data sideBlueSlice = new PieChart.Data("Blue Side", matchDTO.getTimeBallInBlueSide());
        PieChart.Data sideRedSlice = new PieChart.Data("Red Side", matchDTO.getTimeBallInRedSide());
        pieChartTeamSide.getData().add(sideBlueSlice);
        pieChartTeamSide.getData().add(sideRedSlice);

        //get possession information and add it to the possession chart
        PieChart.Data posBlueTeamSlice = new PieChart.Data("Blue Team", matchDTO.getPossessionBlue());
        PieChart.Data posRedTeamSlice = new PieChart.Data("Red Team", matchDTO.getPossessionRed());
        pieChartPossession.getData().add(posBlueTeamSlice);
        pieChartPossession.getData().add(posRedTeamSlice);

        pieChartTeamSide.setStartAngle(90);
        pieChartPossession.setStartAngle(90);
        pieChartTeamSide.setLegendVisible(false);
        pieChartPossession.setLegendVisible(false);

        //set colors
        sideBlueSlice.getNode().setStyle("-fx-pie-color: lightskyblue;");
        sideRedSlice.getNode().setStyle("-fx-pie-color: lightcoral;");
        posBlueTeamSlice.getNode().setStyle("-fx-pie-color: lightskyblue;");
        posRedTeamSlice.getNode().setStyle("-fx-pie-color: lightcoral;");

        //TODO: add heatmap

        LOG.debug("successfully loaded ball statistics");
    }

}
