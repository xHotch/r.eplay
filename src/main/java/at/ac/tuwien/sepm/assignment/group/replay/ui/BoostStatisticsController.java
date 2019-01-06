package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser.BoostInformationParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Boost Statistics Tab Page Controller
 *
 * @author Elias Brugger
 */
@Component
public class BoostStatisticsController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Text pad67, pad12, pad43, pad13, pad66, pad14, pad17, pad5, pad4, pad11, pad18, pad10, pad3, pad41, pad7, pad64, pad40, pad42, pad63, pad23, pad19, pad20, pad31, pad28, pad36, pad21, pad34, pad38, pad68, pad32, pad35, pad65, pad33, pad39;

    @FXML
    private TableView<MatchPlayerDTO> tableTeamBlue;
    @FXML
    private TableView<MatchPlayerDTO> tableTeamRed;

    @FXML
    private TableColumn<MatchPlayerDTO, String> playerNameBlue;
    @FXML
    private TableColumn<MatchPlayerDTO, String> playerNameRed;

    @FXML
    private ImageView mapImage;

    private BoostInformationParser boostInformationParser;

    private Map<Integer, Integer> boostCounts;
    private static final int[] boostIDs = {67,12,43,13,66,18,11,17,5,14,4,10,7,41,3,64,40,42,63,23,19,20,31,28,21,36,68,32,38,34,35,33,65,39};


    public BoostStatisticsController(BoostInformationParser boostInformationParser) {
        this.boostInformationParser = boostInformationParser;
    }

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {
        setUpPlayerTable();
        mapImage.setPreserveRatio(true);
    }

    void loadBoostStatistics(MatchDTO match) {

        //boostInformation.get
        List<MatchPlayerDTO> matchPlayers = match.getPlayerData();

        ObservableList<MatchPlayerDTO> playerListBlue = FXCollections.observableArrayList();
        ObservableList<MatchPlayerDTO> playerListRed = FXCollections.observableArrayList();

        for (MatchPlayerDTO player:matchPlayers) {
            // team blue
            if(player.getTeam() == TeamSide.BLUE){
                playerListBlue.add(player);
            }
            if(player.getTeam() == TeamSide.RED){
                playerListRed.add(player);
            }
        }

        tableTeamBlue.setItems(playerListBlue);
        tableTeamRed.setItems(playerListRed);
        LOG.debug("successfully loaded boost pad statistics");
    }

    void loadBoostPadValues(MatchPlayerDTO player) {
        // key = car ID, value = player ID
        Map<Integer, Integer> carBoostPadMap = boostInformationParser.getCarBoostPadMap();
        Map<Integer, List<BoostPadDTO>> boostPadInformation = boostInformationParser.getBoostPadMap();

        //set up boost counts map everytime a player gets selected
        setupBoostCountsMap();

        int playerID = 0;

        for (Map.Entry<Integer, List<BoostPadDTO>> boostPadInfo:boostPadInformation.entrySet()) {
            if (carBoostPadMap.containsKey(boostPadInfo.getKey())) {
                int boostPadtoPlayerID = carBoostPadMap.get(boostPadInfo.getKey());
                LOG.debug("Car ID {}, Player ID {}", boostPadInfo.getKey(), boostPadtoPlayerID);
                LOG.debug("Current Player {} ID {}", player.getName(), playerID);
                List<BoostPadDTO> boostPadList = boostPadInfo.getValue();
                if (playerID == boostPadtoPlayerID) {
                    for (BoostPadDTO info : boostPadList) {
                        int boostpadID = info.getBoostPadId();

                        int increment = 0;
                        switch (boostpadID) {
                            case 67:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad67.setText(increment + "");
                                break;

                            case 12:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad12.setText(increment + "");
                                break;

                            case 43:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad43.setText(increment + "");
                                break;

                            case 13:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad13.setText(increment + "");
                                break;

                            case 66:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad66.setText(increment + "");
                                break;

                            case 18:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad18.setText(increment + "");
                                break;

                            case 11:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad11.setText(increment + "");
                                break;

                            case 17:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad17.setText(increment + "");
                                break;

                            case 5:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad5.setText(increment + "");
                                break;

                            case 14:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad14.setText(increment + "");
                                break;

                            case 4:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad4.setText(increment + "");
                                break;

                            case 10:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad10.setText(increment + "");
                                break;

                            case 7:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad7.setText(increment + "");
                                break;

                            case 41:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad41.setText(increment + "");
                                break;

                            case 3:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad3.setText(increment + "");
                                break;

                            case 64:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad64.setText(increment + "");
                                break;

                            case 40:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad40.setText(increment + "");
                                break;

                            case 42:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad42.setText(increment + "");
                                break;

                            case 63:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad63.setText(increment + "");
                                break;

                            case 23:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad23.setText(increment + "");
                                break;

                            case 19:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad19.setText(increment + "");
                                break;

                            case 20:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad20.setText(increment + "");
                                break;

                            case 31:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad31.setText(increment + "");
                                break;

                            case 28:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad28.setText(increment + "");
                                break;

                            case 21:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad21.setText(increment + "");
                                break;

                            case 36:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad36.setText(increment + "");
                                break;

                            case 68:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad68.setText(increment + "");
                                break;

                            case 32:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad32.setText(increment + "");
                                break;

                            case 38:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad38.setText(increment + "");
                                break;

                            case 34:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad34.setText(increment + "");
                                break;

                            case 35:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad35.setText(increment + "");
                                break;

                            case 33:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad33.setText(increment + "");
                                break;

                            case 65:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad65.setText(increment + "");
                                break;

                            case 39:
                                increment = boostCounts.get(boostpadID) + 1;
                                boostCounts.put(boostpadID, increment);
                                this.pad39.setText(increment + "");
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper Method to setup up the Player Table Columns
     */
    private void setUpPlayerTable() {
        playerNameBlue.setCellValueFactory(new PropertyValueFactory<>("name"));

        playerNameRed.setCellValueFactory(new PropertyValueFactory<>("name"));

    }

    public void onBlueTableSelection() {
        if(tableTeamBlue.getSelectionModel().getSelectedItem() != null){
            MatchPlayerDTO player = tableTeamBlue.getSelectionModel().getSelectedItem();
            loadBoostPadValues(player);
        }
    }

    public void onRedTableSelection() {
        if(tableTeamRed.getSelectionModel().getSelectedItem() != null){
            MatchPlayerDTO player = tableTeamRed.getSelectionModel().getSelectedItem();
            loadBoostPadValues(player);
        }
    }

    private void setupBoostCountsMap() {
        boostCounts = new HashMap<>();
        for(int i=0; i<boostIDs.length; i++) {
            boostCounts.put(boostIDs[i], 0);
        }
    }

}
