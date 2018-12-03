package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.PlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Main Window Controller.
 *
 * @author Bernhard Bayer
 */
@Component
public class MainwindowController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    AnnotationConfigApplicationContext matchdetailsContext;

    @Autowired
    MatchdetailController matchdetailController;

    @Autowired
    ExecutorService executorService;

    @Autowired
    ReplayService replayService;

    @Autowired
    JsonParseService jsonParseService;

    @Autowired
    MatchService matchService;

    @Autowired
    PlayerService playerService;

    @FXML
    private TableView<MatchDTO> tableViewMatches;
    @FXML
    private TableColumn<MatchDTO, LocalDateTime> tableColumnMatchDate;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnMatchType;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnPlayersBlue;
    @FXML
    private TableColumn<MatchDTO, String> tableColumnPlayersRed;


    public MainwindowController() {

    }

    /**
     * Opens a new Match Detail window.
     *
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


    /**
     * Method that opens a FileChooser when the actionEvent occurs
     * Lets the user chose .replay files
     * <p>
     * The chosen file gets sent to service layer, to parse it
     *
     * @param actionEvent Actionevent from the button
     */
    public void onUploadReplayButtonClicked(ActionEvent actionEvent) {
        LOG.info("Image Chooser clicked");
        LOG.trace("Called - onUploadMatchButtonClicked");

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterREPLAY = new FileChooser.ExtensionFilter("Replay files (*.replay)", "*.REPLAY");
        fileChooser.getExtensionFilters().add(extFilterREPLAY);


        //Show open file dialog
        File inputFile = fileChooser.showOpenDialog(null);
        if (inputFile == null) {
            updateMatchTable();
            LOG.info("File selection cancelled");
            return;
        }

        executorService.submit(() -> {
            try {
                File json = replayService.parseReplayFileToJson(inputFile);
                MatchDTO matchDto = jsonParseService.parseMatch(json);
                LOG.debug("jsonParseFinished");
                for (MatchPlayerDTO mpdto : matchDto.getPlayerData()) {
                    PlayerDTO pdto = new PlayerDTO();
                    pdto.setName(mpdto.getName());
                    pdto.setPlattformid(mpdto.getPlattformId());

                    int id = playerService.createPlayer(pdto);
                    mpdto.setPlayerId(id);
                }
                LOG.debug("All players created");
                matchService.createMatch(matchDto);
                LOG.debug("match created");
            } catch (FileServiceException e) {
                LOG.error("Cought File Service Exception");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (PlayerServiceException e) {
                LOG.error("Cought PlayerServiceException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (PlayerValidationException e) {
                LOG.error("Cought PlayerValidationException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (MatchServiceException e) {
                LOG.error("Cought MatchServiceException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            } catch (MatchValidationException e) {
                LOG.error("Cought MatchValidationException");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            }
        });
        updateMatchTable();
    }


    /**
     * Method to show a simple Error Alert to the user
     *
     * @param errorMessage The String containing the message displayed
     */
    private void showErrorMessage(String errorMessage) {

        LOG.trace("Called - showErrorMessage");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Wrong input error");
        alert.setHeaderText("Error");
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }



    /**
     * Method to update the Match Table
     * Calls the showErrorMessage if an Exception occurs
     */
    private void updateMatchTable(){
        try {
            ObservableList<MatchDTO> observableMatches = FXCollections.observableArrayList(matchService.getMatches());
            SortedList<MatchDTO> sortedMatches = new SortedList<>(observableMatches);
            sortedMatches.comparatorProperty().bind(tableViewMatches.comparatorProperty());
            tableViewMatches.getSortOrder().add(tableColumnMatchDate);
            tableViewMatches.setItems(sortedMatches);
        } catch (MatchServiceException e){
            LOG.error("Cought MatchServiceException {} ", e.getMessage());
            showErrorMessage(e.getMessage());
        }
    }


    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize(){

        setupTable();
        updateMatchTable();

    }

    /**
     * Helper Method to setup up the Table Columns
     */
    private void setupTable(){
        tableColumnMatchDate.setCellValueFactory(
            new PropertyValueFactory<MatchDTO,LocalDateTime>("dateTime"));
        tableColumnPlayersRed.setCellValueFactory(
            new PropertyValueFactory<MatchDTO,String>("teamRedPlayers"));
        tableColumnPlayersBlue.setCellValueFactory(
            new PropertyValueFactory<MatchDTO,String>("teamBluePlayers"));

        tableColumnMatchDate.setSortType(TableColumn.SortType.DESCENDING);
        tableColumnMatchDate.setSortable(true);
        tableViewMatches.sort();
    }



}
