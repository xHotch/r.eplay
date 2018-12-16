package at.ac.tuwien.sepm.assignment.group.replay.ui;


import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * Controller for the Match Detail window
 * @author Bernhard Bayer
 */
@Component
public class MatchDetailController {


    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // Inject tab content.
    @FXML private Tab matchStatsOverviewTab;

    // Inject tab content.
    @FXML private Tab ballStatisticsTab;

}
