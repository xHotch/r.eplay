package at.ac.tuwien.sepm.assignment.group.universe.ui;

import at.ac.tuwien.sepm.assignment.group.universe.exceptions.ServiceException;
import at.ac.tuwien.sepm.assignment.group.universe.service.UniverseService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;

@Component
public class UniverseController {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final double VISIBLE = 1.0;
    private static final double INVISIBLE = 0.0;

    private final FadeTransition fadeOutTransition = new FadeTransition(Duration.seconds(1));
    private final FadeTransition fadeInTransition = new FadeTransition(Duration.seconds(3));

    private final UniverseService universeService;
    private final ExecutorService executorService;

    @FXML
    private Button btnQuestion;

    @FXML
    private Label lblAnswer;

    public UniverseController(UniverseService universeService, ExecutorService executorService) {
        this.universeService = universeService;
        this.executorService = executorService;
        // some transitions for a visually appealing effect
        fadeOutTransition.setFromValue(VISIBLE);
        fadeOutTransition.setToValue(INVISIBLE);
        fadeInTransition.setFromValue(INVISIBLE);
        fadeInTransition.setToValue(VISIBLE);
    }

    @FXML
    private void calculateAnswerButtonPressed() {
        LOG.trace("called calculateAnswerButtonPressed");
        // create a thread and task to prevent ui from freezing on when doing long running operations
        executorService.submit(() -> {
            try {
                String text = universeService.calculateAnswer().getText();
                Platform.runLater(() -> {
                    lblAnswer.setText(text);
                    fadeOutTransition.setNode(btnQuestion);
                    fadeInTransition.setNode(lblAnswer);
                    new ParallelTransition(
                        fadeOutTransition,
                        fadeInTransition
                    ).playFromStart();
                });
            } catch (ServiceException e) {
                LOG.error("could not get answer, {}", e.getMessage(), e);
            }
        });
    }

}
