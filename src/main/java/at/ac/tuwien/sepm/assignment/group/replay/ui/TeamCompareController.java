package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Klampfl
 */
@Controller
public class TeamCompareController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private TeamService teamService;

    private List<MatchDTO> matchDTOList;
    private TeamDTO teamDTO1;
    private TeamDTO teamDTO2;

    @FXML
    private BarChart<String,Double> teamBarChart;
    @FXML
    private ChoiceBox<String> matchValueChoiceBox;

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
    }

    void setTeamCompareData(List<MatchDTO> matchDTOList, TeamDTO teamDTO1, TeamDTO teamDTO2){
        this.matchDTOList = matchDTOList;
        this.teamDTO1 = teamDTO1;
        this.teamDTO2 = teamDTO2;
    }

    private void showMatchValue(int itemIndex) {
        List<XYChart.Series<String,Double>> players = new XYChart.Series<>();

        for (PlayerDTO playerDTO : teamDTO1.getPlayers()) {
        }
        int i = 1;
        for (MatchDTO matchDTO : matchDTOList) {
            for (MatchPlayerDTO matchPlayerDTO : matchDTO.getPlayerData()) {
                double value;
                switch (itemIndex) {
                    case 1:
                        value = matchPlayerDTO.getGoals();
                        break;
                    case 2:
                        value = matchPlayerDTO.getAssists();
                        break;
                    case 3:
                        value = matchPlayerDTO.getShots();
                        break;
                    case 4:
                        value = matchPlayerDTO.getSaves();
                        break;
                    case 5:
                        value = matchPlayerDTO.getScore();
                        break;
                    case 6:
                        value = matchPlayerDTO.getAverageSpeed();
                        break;
                    default:
                        value = 0;
                        break;
                }
                if(player1.getName().equals(matchPlayerDTO.getName())) player1.getData().add(new XYChart.Data<>("Match" + i, value));
                if(player2.getName().equals(matchPlayerDTO.getName())) player2.getData().add(new XYChart.Data<>("Match" + i, value));
                if(player3.getName().equals(matchPlayerDTO.getName())) player3.getData().add(new XYChart.Data<>("Match" + i, value));
                if(player4.getName().equals(matchPlayerDTO.getName())) player4.getData().add(new XYChart.Data<>("Match" + i, value));
                if(player5.getName().equals(matchPlayerDTO.getName())) player5.getData().add(new XYChart.Data<>("Match" + i, value));
                if(player6.getName().equals(matchPlayerDTO.getName())) player6.getData().add(new XYChart.Data<>("Match" + i, value));
            }
            i++;
        }


                if (matchStatsDTO.isTeamDTO()) // TODO team name
                {
                    red.getData().add(new XYChart.Data<>("Match" + i, value));
                } else {
                    blue.getData().add(new XYChart.Data<>("Match" + i, value));
                }
            }
        }

        teamBarChart.getData().add(blue);
        teamBarChart.getData().add(red);
    }
}
