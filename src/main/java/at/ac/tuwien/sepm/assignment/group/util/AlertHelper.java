package at.ac.tuwien.sepm.assignment.group.util;

import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * @author Gabriel Aichinger
 */
public class AlertHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Method to show a simple Error Alert to the user
     *
     * @param errorMessage The String containing the message displayed
     */
    public static void showErrorMessage(String errorMessage) {

        LOG.trace("Called - showErrorMessage");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Wrong input error");
        alert.setHeaderText("Error");
        alert.setContentText(errorMessage);

        alert.showAndWait();
    }
}
