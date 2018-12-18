package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.HeatmapDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
import at.ac.tuwien.sepm.assignment.group.replay.service.PlayerService;
import at.ac.tuwien.sepm.assignment.group.replay.service.ReplayService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.*;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

/**
 * Match Tab Page Controller.
 *
 * @author Daniel Klampfl
 */
@Component
public class MatchController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private SpringFXMLLoader springFXMLLoader;
    //@Autowired
    private MatchStatsOverviewController matchStatsOverviewController;
    private PlayerController playerController;
    private BallStatisticsController ballStatisticsController;
    private MatchPlayerStatisticsController matchPlayerStatisticsController;
    private ExecutorService executorService;
    private ReplayService replayService;
    private JsonParseService jsonParseService;
    private MatchService matchService;
    private PlayerService playerService;

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
    @FXML
    private ProgressIndicator loadReplayProgressIndicator;
    @FXML
    private Button uploadReplayButton;

    public MatchController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService, ReplayService replayService, JsonParseService jsonParseService, MatchService matchService, PlayerService playerService, PlayerController playerController, MatchStatsOverviewController matchStatsOverviewController, BallStatisticsController ballStatisticsController, MatchPlayerStatisticsController matchPlayerStatisticsController) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
        this.replayService = replayService;
        this.jsonParseService = jsonParseService;
        this.matchService = matchService;
        this.playerService = playerService;
        this.playerController = playerController;
        this.matchStatsOverviewController = matchStatsOverviewController;
        this.ballStatisticsController = ballStatisticsController;
        this.matchPlayerStatisticsController = matchPlayerStatisticsController;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {
        setupMatchTable();
        updateMatchTable();
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


        try {
            matchdetailsStage.setScene(new Scene(springFXMLLoader.load("/fxml/matchdetail.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading Match Details fxml failed: " + e.getMessage());
        }

        // load match details for the new window
        if (tableViewMatches.getSelectionModel().getSelectedItem() != null) {
            MatchDTO selectedMatch = tableViewMatches.getSelectionModel().getSelectedItem();
            matchStatsOverviewController.loadBasicMatchData(selectedMatch);
            ballStatisticsController.loadBallStatistics(selectedMatch);
            matchPlayerStatisticsController.loadMatchPlayerStatistics(selectedMatch);
            // show application
            matchdetailsStage.show();
            matchdetailsStage.toFront();
            LOG.debug("Opening Match Details window complete");
        } else {
            AlertHelper.showErrorMessage("No match selected");
        }

    }

    public void onMatchDeleteButtonClicked(ActionEvent actionEvent) {
        LOG.info("Match delete button clicked");
        LOG.trace("called - onMatchDeleteButtonClicked");
        if (tableViewMatches.getSelectionModel().getSelectedItem() != null) {
            MatchDTO selectedMatch = tableViewMatches.getSelectionModel().getSelectedItem();
            try {
                matchService.deleteMatch(selectedMatch);
                updateMatchTable();
            } catch (MatchServiceException e) {
                LOG.error("caught MatchServiceException", e);
                AlertHelper.showErrorMessage(e.getMessage());
            }
        } else {
            AlertHelper.showErrorMessage("No match selected");
        }
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
        LOG.info("Upload Replay Button clicked");
        LOG.trace("Called - onUploadMatchButtonClicked");

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterREPLAY = new FileChooser.ExtensionFilter("Replay files (*.replay)", "*.REPLAY");
        fileChooser.getExtensionFilters().add(extFilterREPLAY);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "qse01ReplayParser/files"));

        //Show open file dialog
        File inputFile = fileChooser.showOpenDialog(null);
        if (inputFile == null) {
            LOG.info("File selection cancelled");
            return;
        }

        executorService.submit(() -> {
            File json;
            try {
                Platform.runLater(() -> loadReplayProgressIndicator.setVisible(true));
                Platform.runLater(() -> uploadReplayButton.setDisable(true));
                json = replayService.parseReplayFileToJson(inputFile);
                MatchDTO matchDto;
                try {
                    matchDto = jsonParseService.parseMatch(json);
                } finally {
                    matchService.deleteFile(json);
                }
                LOG.debug("jsonParseFinished");
                for (MatchPlayerDTO mpdto : matchDto.getPlayerData()) {
                    playerService.createPlayer(mpdto.getPlayerDTO());
                }
                LOG.debug("All players created");
                matchService.createMatch(matchDto);
                LOG.debug("match created");
                Platform.runLater(() -> {
                    updateMatchTable();
                    playerController.updatePlayerTable();
                });
            } catch (FileServiceException e) {
                LOG.error("Caught File Service Exception", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            } catch (PlayerServiceException e) {
                LOG.error("Caught PlayerServiceException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            } catch (PlayerValidationException e) {
                LOG.error("Caught PlayerValidationException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            } catch (MatchServiceException e) {
                LOG.error("Caught MatchServiceException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            } catch (MatchValidationException e) {
                LOG.error("Caught MatchValidationException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            } catch (ReplayAlreadyExistsException e) {
                LOG.error("Caught ReplayAlreadyExistsException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            } catch (Exception e){
                LOG.error("Caught Exception ##############", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage(e.getMessage()));
            }finally {
                Platform.runLater(() -> loadReplayProgressIndicator.setVisible(false));
                Platform.runLater(() -> uploadReplayButton.setDisable(false));
            }

        });

    }

    /**
     * Method to update the Match Table
     * Calls the showErrorMessage if an Exception occurs
     */
    private void updateMatchTable() {
        try {
            ObservableList<MatchDTO> observableMatches = FXCollections.observableArrayList(matchService.getMatches());
            SortedList<MatchDTO> sortedMatches = new SortedList<>(observableMatches);
            sortedMatches.comparatorProperty().bind(tableViewMatches.comparatorProperty());
            tableViewMatches.getSortOrder().add(tableColumnMatchDate);
            tableViewMatches.setItems(sortedMatches);
        } catch (MatchServiceException e) {
            LOG.error("Caught MatchServiceException {} ", e.getMessage());
            AlertHelper.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Helper Method to setup up the Match Table Columns
     */
    private void setupMatchTable() {
        tableColumnMatchDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        tableColumnPlayersRed.setCellValueFactory(new PropertyValueFactory<>("teamRedPlayers"));
        tableColumnPlayersBlue.setCellValueFactory(new PropertyValueFactory<>("teamBluePlayers"));

        tableColumnMatchDate.setSortType(TableColumn.SortType.DESCENDING);
        tableColumnMatchDate.setSortable(true);
        tableViewMatches.sort();
    }


}

