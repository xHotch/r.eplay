package at.ac.tuwien.sepm.assignment.group.replay.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
public class MatchdetailController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Label label_gameMode;

//    private String s;

    public MatchdetailController() {
    }

//    public void setString(String s){
//        //this.s = s;
//        label_gameMode.setText(s);
//    }

//    public void button(ActionEvent actionEvent) {
//        label_gameMode.setText(s);
//    }
}
