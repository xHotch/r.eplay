package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamDTO;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;

@Component
public class TeamController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    private ExecutorService executorService;

    @FXML
    private TableView<TeamDTO> tableViewTeams;
    @FXML
    private TableColumn<TeamDTO, String> tableColumnTeamName;

    public TeamController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService) {
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

    public void onNewTeamButtonClicked(ActionEvent actionEvent) {
        Stage newTeamStage = new Stage();
        // setup application
        newTeamStage.setTitle("Matchdetails");
        newTeamStage.setWidth(1024);
        newTeamStage.setHeight(768);
        newTeamStage.centerOnScreen();
        newTeamStage.setOnCloseRequest(event -> {
            LOG.debug("Match Details window closed");
        });


        try {
            newTeamStage.setScene(new Scene(springFXMLLoader.load("/fxml/newTeam.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading New Team fxml failed: " + e.getMessage());
        }
        newTeamStage.toFront();
        newTeamStage.showAndWait();

    }
}
