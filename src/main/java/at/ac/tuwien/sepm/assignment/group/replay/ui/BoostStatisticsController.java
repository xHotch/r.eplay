package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
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
import java.util.ArrayList;
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

        Map<Integer, List<BoostDTO>> boosts = boostInformationParser.getBoostAmountMap();

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

        List<Integer> boostPadList = player.getDBBoostPadMap().get((int)player.getPlayerId());
        //set up boost counts map everytime a player gets selected
        setupBoostCountsMap();

        pad67.setText(boostPadList.get(0) + "");
        pad12.setText(boostPadList.get(1) + "");
        pad43.setText(boostPadList.get(2) + "");
        pad13.setText(boostPadList.get(3) + "");
        pad66.setText(boostPadList.get(4) + "");
        pad18.setText(boostPadList.get(5) + "");
        pad11.setText(boostPadList.get(6) + "");
        pad17.setText(boostPadList.get(7) + "");
        pad5.setText(boostPadList.get(8) + "");
        pad14.setText(boostPadList.get(9) + "");
        pad4.setText(boostPadList.get(10) + "");
        pad10.setText(boostPadList.get(11) + "");
        pad7.setText(boostPadList.get(12) + "");
        pad41.setText(boostPadList.get(13) + "");
        pad3.setText(boostPadList.get(14) + "");
        pad64.setText(boostPadList.get(15) + "");
        pad40.setText(boostPadList.get(16) + "");
        pad42.setText(boostPadList.get(17) + "");
        pad63.setText(boostPadList.get(18) + "");
        pad23.setText(boostPadList.get(19) + "");
        pad19.setText(boostPadList.get(20) + "");
        pad20.setText(boostPadList.get(21) + "");
        pad31.setText(boostPadList.get(22) + "");
        pad28.setText(boostPadList.get(23) + "");
        pad21.setText(boostPadList.get(24) + "");
        pad36.setText(boostPadList.get(25) + "");
        pad68.setText(boostPadList.get(26) + "");
        pad32.setText(boostPadList.get(27) + "");
        pad38.setText(boostPadList.get(28) + "");
        pad34.setText(boostPadList.get(29) + "");
        pad35.setText(boostPadList.get(30) + "");
        pad33.setText(boostPadList.get(31) + "");
        pad65.setText(boostPadList.get(32) + "");
        pad39.setText(boostPadList.get(33) + "");

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
