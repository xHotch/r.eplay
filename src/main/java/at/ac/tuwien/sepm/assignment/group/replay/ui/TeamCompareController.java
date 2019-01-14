package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchStatsDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.TeamService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
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

    private Map<Integer, List<MatchStatsDTO>> matchPerTeamStats1;
    private Map<Integer, List<MatchStatsDTO>> matchPerTeamStats2;

    @FXML
    private BarChart<Integer,Double> team1BarChart;
    @FXML
    private BarChart<Integer,Double> team2BarChart;
    @FXML
    private ChoiceBox<String> matchValueChoiceBox;

    public TeamCompareController(TeamService teamService) {
        this.teamService = teamService;
    }

    @FXML
    private void initialize()
    {

    }

    void setTeamCompareData(Map<Integer, List<MatchStatsDTO>> matchPerTeamStats1,Map<Integer, List<MatchStatsDTO>> matchPerTeamStats2){
        this.matchPerTeamStats1 = matchPerTeamStats1;
        this.matchPerTeamStats2 = matchPerTeamStats2;
    }

    private void loadMatchValue()
    {

    }
}
