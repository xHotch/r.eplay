package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ChoiceBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Daniel Klampfl
 */
@Controller
public class TeamCompareController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TeamService teamService;

    private TeamDTO teamDTO1;
    private TeamDTO teamDTO2;

    @FXML
    private BarChart<String,Double> teamBarChart;
    @FXML
    private CategoryAxis teamCategoryAxis;
    @FXML
    private NumberAxis teamNumberAxis;
    @FXML
    private ChoiceBox<String> matchValueChoiceBox;
    private TeamCompareDTO teamCompareDTO;

    public TeamCompareController(TeamService teamService) {
        this.teamService = teamService;
    }

    @FXML
    private void initialize()
    {
        matchValueChoiceBox.getItems().addAll("Tore","Assists","Shots","Saves","Score","Average Speed"); //TODO German names
        matchValueChoiceBox.getSelectionModel().selectedIndexProperty().addListener(
            (obs, oldValue, newValue) -> showMatchValue(newValue.intValue())
        );
        teamBarChart.setAnimated(false);
        teamNumberAxis.setAutoRanging(true);
    }

    void setTeamCompareData(TeamCompareDTO teamCompareDTO, TeamDTO teamDTO1, TeamDTO teamDTO2){
        teamCompareDTO.setMatchStatsDTOList(teamCompareDTO.getMatchStatsDTOList().entrySet().stream()
            .filter(e -> teamCompareDTO.getMatchDTOList().stream().anyMatch(m -> m.getId() == e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        this.teamCompareDTO = teamCompareDTO;
        this.teamDTO1 = teamDTO1;
        this.teamDTO2 = teamDTO2;
    }

    private void showMatchValue(int itemIndex) {
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
                if(matchStatsDTO.getTeam() == TeamSide.RED) team1.getData().add(new XYChart.Data<>("Match " + i,value));
                else team2.getData().add(new XYChart.Data<>("Match " + i,value));
            }
            i++;
        }
        teamBarChart.getData().add(team1);
        teamBarChart.getData().add(team2);

        //set color for bars depending on team
        for (XYChart.Data data : team1.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #246dfa;");
        }
        for (XYChart.Data data : team2.getData()) {
            data.getNode().setStyle("-fx-bar-fill: #f04555;");
        }
    }
}
