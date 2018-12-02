package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Main Window Controller.
 * @author Bernhard Bayer
 */
@Component
public class MainwindowController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    AnnotationConfigApplicationContext matchdetailsContext;

    @Autowired
    MatchdetailController matchdetailController;


    public MainwindowController() {

    }

    /**
     * Opens a new Match Detail window.
     * @param actionEvent Actionevent from the button
     */
    public void onMatchdetailsButtonClicked(ActionEvent actionEvent) {

        LOG.info("Match Details button clicked");
        LOG.debug("Opening Match Details window");

        Stage matchdetailsStage = new Stage();
        // setup application
        matchdetailsStage.setTitle("Matchdetails");
        matchdetailsStage.setWidth(1024);
        matchdetailsStage.setHeight(768);
        matchdetailsStage.centerOnScreen();
        matchdetailsStage.setOnCloseRequest(event -> {
            LOG.debug("Match Details window closed");
        });

        // prepare fxml loader to inject controller
        SpringFXMLLoader springFXMLLoader = matchdetailsContext.getBean(SpringFXMLLoader.class);

        try {
            matchdetailsStage.setScene(new Scene(springFXMLLoader.load("/fxml/matchdetail.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading Match Details fxml failed: " + e.getMessage());
        }

        // show application
        matchdetailsStage.show();
        matchdetailsStage.toFront();
        LOG.debug("Opening Match Details window complete");

        //Get data to match detail window
        //To get data to match detail window create a setter method in MatchdetailController and call it here
        //e.g.: matchdetailController.setString("test");

        //TODO

    }
}
