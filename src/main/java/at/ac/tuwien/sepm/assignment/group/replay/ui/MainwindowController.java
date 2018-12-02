package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.exception.MatchServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.MatchService;
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
    MatchService matchService;

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
            LOG.info("File selection cancelled");
            return;
        }

        executorService.submit(() -> {
            try {
                replayService.parseReplayFileToJson(inputFile);

            } catch (FileServiceException e) {
                LOG.error("Cought File Service Exception");
                Platform.runLater(() -> showErrorMessage(e.getMessage()));
            }
        });
    }

    public void onAddTestButtonClicked(ActionEvent actionEvent){
        try {
            matchService.createMatch(addTestMatch());
        } catch (Exception e){

        }
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

    private void updateMatchTable(){
        try {
            ObservableList<MatchDTO> observableMatches = FXCollections.observableArrayList(matchService.getMatches());
            SortedList<MatchDTO> sortedMatches = new SortedList<>(observableMatches);
            sortedMatches.comparatorProperty().bind(tableViewMatches.comparatorProperty());
            tableViewMatches.getSortOrder().add(tableColumnMatchDate);
            tableViewMatches.setItems(sortedMatches);
        } catch (MatchServiceException e){

        }
    }

    @FXML
    void initialize(){

        setupTable();

        updateMatchTable();

    }

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


    private MatchDTO addTestMatch(){

        // set up a match entity and define the object variables
        MatchDTO matchDTO = new MatchDTO();

        // set the time
        matchDTO.setDateTime(LocalDate.now().atStartOfDay());

        // add 2 players to the match list ... simulating a 1v1 match
        List<MatchPlayerDTO> playerMatchList = new LinkedList<>();

        // create 2 players
        MatchPlayerDTO playerRED = new MatchPlayerDTO();
        MatchPlayerDTO playerBLUE = new MatchPlayerDTO();

        // helper method to fill the player fields
        setPlayerVariables(playerRED, 1, "Player 1", 0, 10, 2,3, 5, 1);
        setPlayerVariables(playerBLUE, 2, "Player 2", 1, 15, 4,2, 3, 7);

        playerMatchList.add(playerRED);
        playerMatchList.add(playerBLUE);
        matchDTO.setPlayerData(playerMatchList);

        // set the remaining match variables
        matchDTO.setTeamBlueGoals(2);
        matchDTO.setTeamRedGoals(4);
        matchDTO.setTeamSize(1);

        return matchDTO;
    }

    public void setPlayerVariables(MatchPlayerDTO player, int id, String name, int team, int score, int goals, int assists, int shots, int saves){
        player.setId(id);
        player.setName(name);
        player.setTeam(team);
        player.setScore(score);
        player.setGoals(goals);
        player.setAssists(assists);
        player.setShots(shots);
        player.setSaves(saves);
    }



}
