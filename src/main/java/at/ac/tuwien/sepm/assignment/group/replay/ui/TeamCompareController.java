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
    }
}
