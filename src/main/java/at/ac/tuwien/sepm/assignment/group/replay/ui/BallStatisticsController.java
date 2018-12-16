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

    public void loadBallStatistics(MatchDTO matchDTO){

        pieChartTeamSide = new PieChart();
        pieChartPossession = new PieChart();

        //get ball info, set slice color and add it to the team side chart
        PieChart.Data sideBlueSlice = new PieChart.Data("Blue Side", matchDTO.getTimeBallInBlueSide());
        PieChart.Data sideRedSlice = new PieChart.Data("Red Side", matchDTO.getTimeBallInRedSide());
        //TODO: set color of slices
        pieChartTeamSide.getData().add(sideBlueSlice);
        pieChartTeamSide.getData().add(sideRedSlice);

        //get possession information and add it to the possession chart
        PieChart.Data posBlueTeamSlice = new PieChart.Data("Blue Team", matchDTO.getPossessionBlue());
        PieChart.Data posRedTeamSlice = new PieChart.Data("Red Team", matchDTO.getPossessionRed());
        pieChartPossession.getData().add(posBlueTeamSlice);
        pieChartPossession.getData().add(posRedTeamSlice);


        //pieChartTeamSide.setLegendVisible(false);
        //pieChartPossession.setLegendVisible(false);

    }

}
