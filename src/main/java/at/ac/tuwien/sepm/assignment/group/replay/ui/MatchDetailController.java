package at.ac.tuwien.sepm.assignment.group.replay.ui;


import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import org.springframework.stereotype.Component;

/**
 * Controller for the Match Detail window
 * @author Bernhard Bayer
 */
@Component
public class MatchDetailController {

    private MatchAnimationController matchAnimationController;

    public MatchDetailController(MatchAnimationController matchAnimationController) {
        this.matchAnimationController = matchAnimationController;
    }

    // Inject tab content.
    @FXML private Tab matchStatsOverviewTab;

    // Inject tab content.
    @FXML private Tab ballStatisticsTab;

    @FXML private Tab playerPositionTab;

    @FXML private Tab matchAnimationTab;

    // Inject tab content.
    @FXML private Tab boostStatisticsTab;

    @FXML
    private void initialize(){
        matchAnimationTab.setOnSelectionChanged (e -> {
            if (!matchAnimationTab.isSelected()) matchAnimationController.pauseAnimation();
        }
        );
    }

}
