package at.ac.tuwien.sepm.assignment.group.replay.ui;

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
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private BoostStatisticsController boostStatisticsController;
    private MatchAnimationController matchAnimationController;
    private ExecutorService executorService;
    private ReplayService replayService;
    private JsonParseService jsonParseService;
    private MatchService matchService;
    private PlayerService playerService;
    private MatchCompareController matchCompareController;

    private boolean filter = false;
    private boolean filterError = false;

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
    @FXML
    private ChoiceBox<String> choiceBoxMatchtyp;
    @FXML
    private TextField nameTextField;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private CheckBox nameCheckBox;
    @FXML
    private CheckBox timeCheckBox;
    @FXML
    private CheckBox typCheckBox;


    public MatchController(SpringFXMLLoader springFXMLLoader, ExecutorService executorService, ReplayService replayService, JsonParseService jsonParseService, MatchService matchService, PlayerService playerService, PlayerController playerController, MatchStatsOverviewController matchStatsOverviewController, BallStatisticsController ballStatisticsController, BoostStatisticsController boostStatisticsController, MatchPlayerStatisticsController matchPlayerStatisticsController, MatchAnimationController matchAnimationController, MatchCompareController matchCompareController) {
        this.springFXMLLoader = springFXMLLoader;
        this.executorService = executorService;
        this.replayService = replayService;
        this.jsonParseService = jsonParseService;
        this.matchService = matchService;
        this.playerService = playerService;
        this.playerController = playerController;
        this.matchStatsOverviewController = matchStatsOverviewController;
        this.ballStatisticsController = ballStatisticsController;
        this.boostStatisticsController = boostStatisticsController;
        this.matchPlayerStatisticsController = matchPlayerStatisticsController;
        this.matchAnimationController = matchAnimationController;
        this.matchCompareController = matchCompareController;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    private void initialize() {
        tableViewMatches.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        choiceBoxMatchtyp.getItems().addAll("1 vs 1", "2 vs 2", "3 vs 3");
        setupMatchTable();
        updateMatchTable();
    }

    @FXML
    private void onSearchButtonClicked() {
        LOG.info("Search button clicked");
        filterError = false;
        filter = true;
        updateMatchTable();
    }

    @FXML
    private void onRevertSearchButtonClicked() {
        LOG.info("Revert Search button clicked");
        filter = false;
        filterError = false;
        nameTextField.setText("");
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        choiceBoxMatchtyp.setValue(null);
        typCheckBox.setSelected(false);
        timeCheckBox.setSelected(false);
        nameCheckBox.setSelected(false);
        updateMatchTable();
    }


    /**
     * Opens a new Match Detail window.
     */
    @FXML
    private void onMatchdetailsButtonClicked() {

        LOG.info("Match Details button clicked");
        LOG.debug("Opening Match Details window");

        Stage matchdetailsStage = new Stage();
        // setup application
        matchdetailsStage.setTitle("Matchdetails");
        matchdetailsStage.setWidth(1024);
        matchdetailsStage.setHeight(1024);
        matchdetailsStage.getIcons().add(new Image("/images/ballIcon.png"));
        matchdetailsStage.centerOnScreen();
        matchdetailsStage.setOnCloseRequest(event -> LOG.debug("Match Details window closed"));


        try {
            matchdetailsStage.setScene(new Scene(springFXMLLoader.load("/fxml/matchdetail.fxml", Parent.class)));
        } catch (IOException e) {
            LOG.error("Loading Match Details fxml failed", e);
            AlertHelper.showErrorMessage("Fenster mit den Match Details konnte nicht geöffnet werden.");
        }

        // load match details for the new window
        if (tableViewMatches.getSelectionModel().getSelectedItems().size() == 1) {
            MatchDTO selectedMatch = tableViewMatches.getSelectionModel().getSelectedItem();
            try {
                matchService.getHeatmaps(selectedMatch);
            } catch (FileServiceException e) {
                LOG.error("Could not read Heatmaps", e);
            }
            matchStatsOverviewController.loadBasicMatchData(selectedMatch);
            ballStatisticsController.loadBallStatistics(selectedMatch);
            matchPlayerStatisticsController.loadMatchPlayerStatistics(selectedMatch);
            boostStatisticsController.loadBoostStatistics(selectedMatch);
            matchAnimationController.setMatchDTO(selectedMatch);
            // show application
            matchdetailsStage.show();
            matchdetailsStage.toFront();
            LOG.debug("Opening Match Details window complete");
        } else {
            AlertHelper.showErrorMessage("Wählen Sie genau ein Match aus");
        }

    }

    @FXML
    private void onMatchDeleteButtonClicked() {
        LOG.info("Match delete button clicked");
        LOG.trace("called - onMatchDeleteButtonClicked");
        if (tableViewMatches.getSelectionModel().getSelectedItems().size() == 1) {
            MatchDTO selectedMatch = tableViewMatches.getSelectionModel().getSelectedItem();

            //let the user confirm the deletion
            String matchName = selectedMatch.getFormattedDateTime() + ", " + selectedMatch.getMatchType() + ", " + selectedMatch.getTeamBluePlayers() + ", " + selectedMatch.getTeamRedPlayers();
            Optional<ButtonType> result = AlertHelper.alert(Alert.AlertType.CONFIRMATION,"Lösche Match",null,"Bist du dir sicher, dass du folgendes Match löschen möchtest? \n" + matchName);

            if (result.get() == ButtonType.OK) {
                try {
                    matchService.deleteMatch(selectedMatch);
                    updateMatchTable();
                } catch (MatchServiceException e) {
                    LOG.error("caught MatchServiceException", e);
                    AlertHelper.showErrorMessage("Fehler beim Löschen des Matches");
                }
            }
        } else {
            AlertHelper.showErrorMessage("Wählen Sie genau ein match aus");
        }
    }

    @FXML
    private void onMatchCompareButtonClicked() {
        List<MatchDTO> selectedMatches = tableViewMatches.getSelectionModel().getSelectedItems();
        if (selectedMatches.size() == 2) {
            Stage matchCompareStage = new Stage();
            // setup application
            matchCompareStage.setTitle("Matchvergleich");
            matchCompareStage.setWidth(1024);
            matchCompareStage.setHeight(768);
            matchCompareStage.getIcons().add(new Image("/images/ballIcon.png"));
            matchCompareStage.centerOnScreen();
            matchCompareStage.setOnCloseRequest(event -> LOG.debug("Select team matches window closed"));
            try {
                matchCompareStage.setScene(new Scene(springFXMLLoader.load("/fxml/matchComparePage.fxml", Parent.class)));
            } catch (IOException e) {
                LOG.error("Loading compareMatches fxml failed", e);
                AlertHelper.showErrorMessage("Fenster zum Vergleichen der Matches konnte nicht geöffnet werden.");
            }
            matchCompareController.setUp(selectedMatches.get(0), selectedMatches.get(1));
            matchCompareStage.toFront();
            matchCompareStage.showAndWait();
        } else {
            AlertHelper.alert(Alert.AlertType.INFORMATION, "Info", null, "Es müssen genau zwei Matches ausgewählt werden.");
        }
    }

    /**
     * Method that opens a FileChooser when the actionEvent occurs
     * Lets the user chose .replay files
     * <p>
     * The chosen file gets sent to service layer, to parse it
     */
    @FXML
    private void onUploadReplayButtonClicked() {
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
            File json = null;
            File replayFile = null;
            try {
                Platform.runLater(() -> loadReplayProgressIndicator.setVisible(true));
                Platform.runLater(() -> uploadReplayButton.setDisable(true));
                replayFile = replayService.copyReplayFile(inputFile);
                MatchDTO matchDto;
                try {
                    json = replayService.parseReplayFileToJson(replayFile);
                    matchDto = jsonParseService.parseMatch(json);
                    matchDto.setReplayFilename(replayFile.getName());
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
            } catch (PlayerServiceException e) {
                LOG.error("Caught PlayerServiceException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage("Fehler beim Speichern des Replays: Fehler beim Speichern der Spieler"));
            } catch (PlayerValidationException e) {
                LOG.error("Caught PlayerValidationException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage("Fehler beim Speichern des Replays: Ungültige Spieler: " + e.getMessage()));
            } catch (MatchServiceException e) {
                LOG.error("Caught MatchServiceException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage("Fehler beim Speichern des Replays: Fehler beim Speichern des Matches"));
            } catch (MatchValidationException e) {
                LOG.error("Caught MatchValidationException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage("Fehler beim Speichern des Replays: Ungültiges Match: " + e.getMessage()));
            } catch (ReplayAlreadyExistsException e) {
                try {
                    matchService.deleteFile(replayFile);
                } catch (FileServiceException e1) {
                    LOG.error("Could not delete replay file", e1);
                }
                LOG.error("Caught ReplayAlreadyExistsException", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage("Fehler beim Speichern des Replays: Replay existiert bereits"));
            } catch (FileServiceException e) {
                try {
                    matchService.deleteFile(replayFile);
                } catch (FileServiceException e1) {
                    LOG.error("Could not delete replay file", e1);
                }
                LOG.error("Caught File Service Exception", e);
                Platform.runLater(() -> AlertHelper.showErrorMessage("Fehler beim Speichern des Replays"));
            } catch (Exception e){
                LOG.error("Caught Exception ", e);
            } finally {
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
            ObservableList<MatchDTO> observableMatches;
            if (!filter || filterError) {
                observableMatches = FXCollections.observableArrayList(matchService.getMatches());
            } else {
                String name = null;
                LocalDateTime begin = null;
                LocalDateTime end = null;
                int teamSize = 0;
                if (nameCheckBox.isSelected()) {
                    name = nameTextField.getText();
                    if (name.equals("")) {
                        name = null;
                    }
                }
                if (timeCheckBox.isSelected()) {
                    if (fromDatePicker.getValue() != null) {
                        begin = fromDatePicker.getValue().atStartOfDay();
                    }
                    if (toDatePicker.getValue() != null) {
                        end = toDatePicker.getValue().atStartOfDay().plusDays(1).minusSeconds(1);
                    }
                }
                if (typCheckBox.isSelected() && choiceBoxMatchtyp.getValue() != null) {
                    teamSize = getMatchtypeByChoiceBoxValue();
                }
                observableMatches = FXCollections.observableArrayList(matchService.searchMatches(name, begin, end, teamSize));
                filterError = false;
            }

            SortedList<MatchDTO> sortedMatches = new SortedList<>(observableMatches);
            sortedMatches.comparatorProperty().bind(tableViewMatches.comparatorProperty());
            tableViewMatches.getSortOrder().add(tableColumnMatchDate);
            tableViewMatches.setItems(sortedMatches);
        } catch (MatchServiceException e) {
            LOG.error("Caught MatchServiceException", e);
            AlertHelper.showErrorMessage("Fehler beim laden der Matches");
        } catch (FilterValidationException e) {
            filterError = true;
            LOG.error("Caught FilterValidationException", e);
            AlertHelper.showErrorMessage(e.getMessage());
        }
    }

    private int getMatchtypeByChoiceBoxValue() {
        String matchtype = choiceBoxMatchtyp.getValue();
        int matchtypeInt = 0;

        switch (matchtype) {
            case "1 vs 1":
                matchtypeInt = 1;
                break;
            case "2 vs 2":
                matchtypeInt = 2;
                break;
            case "3 vs 3":
                matchtypeInt = 3;
                break;
        }
        return matchtypeInt;
    }

    /**
     * Helper Method to setup up the Match Table Columns
     */
    private void setupMatchTable() {
        tableColumnMatchDate.setCellValueFactory(new PropertyValueFactory<>("formattedDateTime"));
        tableColumnPlayersRed.setCellValueFactory(new PropertyValueFactory<>("teamRedPlayers"));
        tableColumnPlayersBlue.setCellValueFactory(new PropertyValueFactory<>("teamBluePlayers"));
        tableColumnMatchType.setCellValueFactory(new PropertyValueFactory<>("matchType"));

        tableColumnMatchDate.setStyle("-fx-alignment: CENTER;");
        tableColumnMatchType.setStyle("-fx-alignment: CENTER;");
        tableColumnPlayersRed.setStyle("-fx-alignment: CENTER;");
        tableColumnPlayersBlue.setStyle("-fx-alignment: CENTER;");

        tableColumnMatchDate.setSortType(TableColumn.SortType.DESCENDING);
        tableColumnMatchDate.setSortable(true);
        tableViewMatches.sort();
    }
}