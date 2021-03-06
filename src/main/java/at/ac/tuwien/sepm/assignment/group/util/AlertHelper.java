package at.ac.tuwien.sepm.assignment.group.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

/**
 * @author Gabriel Aichinger
 */
public class AlertHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private AlertHelper() {
        throw new IllegalStateException("AlertHelper class");
    }
    /**
     * Method to show a simple Error Alert to the user
     *
     * @param errorMessage The String containing the message displayed
     */
    public static void showErrorMessage(String errorMessage) {

        LOG.trace("Called - showErrorMessage");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Eingabefehler"/*"Wrong input error"*/);
        alert.setHeaderText("Fehler");
        alert.setContentText(errorMessage);
        alert.getDialogPane().getStylesheets().add("/css/mainTemplate.css");

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
        alert.getDialogPane().getStylesheets().add("/css/mainTemplate.css");

        return alert.showAndWait();
    }
}
