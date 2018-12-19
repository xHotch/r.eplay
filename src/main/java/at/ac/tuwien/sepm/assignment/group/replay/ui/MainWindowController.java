package at.ac.tuwien.sepm.assignment.group.replay.ui;



import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;

/**
 * Main Window Controller.
 *
 * @author Bernhard Bayer
 */
@Component
public class MainWindowController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private ExecutorService executorService;

    // Inject tab content.
    @FXML private Tab matchTab;
    // Inject controller
    //@FXML private MatchController matchTabPageController;

    // Inject tab content.
    @FXML private Tab playerTab;
    // Inject controller
    //@FXML private PlayerController playerTabPageController;

    @FXML private Tab teamTab;

    public MainWindowController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {

    }

}
