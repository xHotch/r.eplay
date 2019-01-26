package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
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
import java.util.List;

/**
 * Boost Statistics Tab Page Controller
 *
 * @author Elias Brugger
 */
@Component
public class BoostStatisticsController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Text pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7, pad8, pad9, pad10, pad11, pad12, pad13, pad14, pad15, pad16, pad17, pad18, pad19, pad20, pad21, pad22, pad23, pad24, pad25, pad26, pad27, pad28, pad29, pad30, pad31, pad32, pad33;

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

    /**
     * FXML Initialize method.
     * Calls methods to setup and update table;
     */
    @FXML
    void initialize() {
        setUpPlayerTable();
        mapImage.setPreserveRatio(true);
        tableTeamBlue.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldPlayer, newPlayer) -> onBlueTableSelection(newPlayer)
        );
        tableTeamRed.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldPlayer, newPlayer) -> onRedTableSelection(newPlayer)
        );
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
        tableTeamBlue.getSelectionModel().select(0);
        LOG.debug("successfully loaded boost pad statistics");
    }

    void loadBoostPadValues(MatchPlayerDTO player) {

        List<Integer> boostPadList = player.getDBBoostPadMap().get((int)player.getPlayerId());

        pad0.setText(boostPadList.get(0) + "");
        pad1.setText(boostPadList.get(1) + "");
        pad2.setText(boostPadList.get(2) + "");
        pad3.setText(boostPadList.get(3) + "");
        pad4.setText(boostPadList.get(4) + "");
        pad5.setText(boostPadList.get(5) + "");
        pad6.setText(boostPadList.get(6) + "");
        pad7.setText(boostPadList.get(7) + "");
        pad8.setText(boostPadList.get(8) + "");
        pad9.setText(boostPadList.get(9) + "");
        pad10.setText(boostPadList.get(10) + "");
        pad11.setText(boostPadList.get(11) + "");
        pad12.setText(boostPadList.get(12) + "");
        pad13.setText(boostPadList.get(13) + "");
        pad14.setText(boostPadList.get(14) + "");
        pad15.setText(boostPadList.get(15) + "");
        pad16.setText(boostPadList.get(16) + "");
        pad17.setText(boostPadList.get(17) + "");
        pad18.setText(boostPadList.get(18) + "");
        pad19.setText(boostPadList.get(19) + "");
        pad20.setText(boostPadList.get(20) + "");
        pad21.setText(boostPadList.get(21) + "");
        pad22.setText(boostPadList.get(22) + "");
        pad23.setText(boostPadList.get(23) + "");
        pad24.setText(boostPadList.get(24) + "");
        pad25.setText(boostPadList.get(25) + "");
        pad26.setText(boostPadList.get(26) + "");
        pad27.setText(boostPadList.get(27) + "");
        pad28.setText(boostPadList.get(28) + "");
        pad29.setText(boostPadList.get(29) + "");
        pad30.setText(boostPadList.get(30) + "");
        pad31.setText(boostPadList.get(31) + "");
        pad32.setText(boostPadList.get(32) + "");
        pad33.setText(boostPadList.get(33) + "");

    }

    /**
     * Helper Method to setup up the Player Table Columns
     */
    private void setUpPlayerTable() {
        playerNameBlue.setCellValueFactory(new PropertyValueFactory<>("name"));

        playerNameRed.setCellValueFactory(new PropertyValueFactory<>("name"));

    }

    private void onBlueTableSelection(MatchPlayerDTO matchPlayerDTO) {
        if(matchPlayerDTO != null){
            tableTeamRed.getSelectionModel().clearSelection();
            loadBoostPadValues(matchPlayerDTO);
        }
    }

    private void onRedTableSelection(MatchPlayerDTO matchPlayerDTO) {
        if(matchPlayerDTO != null){
            tableTeamBlue.getSelectionModel().clearSelection();
            loadBoostPadValues(matchPlayerDTO);
        }
    }


}
