package at.ac.tuwien.sepm.assignment.group.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

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

    /**
     * General alert method to show information to the user
     * @param alertType typ of the alert
     * @param title window title
     * @param header header text ,can be null for no text
     * @param context the message for the user
     * @return the buttonType Pressed by the User
     */
    public static Optional<ButtonType> alert(Alert.AlertType alertType, String title, String header, String context) {
        LOG.trace("Called - alert");

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(context);

        return alert.showAndWait();
    }
}
