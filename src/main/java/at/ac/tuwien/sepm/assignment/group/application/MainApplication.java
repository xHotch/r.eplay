package at.ac.tuwien.sepm.assignment.group.application;

import at.ac.tuwien.sepm.assignment.group.util.SpringFXMLLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

@Component
@ComponentScan("at.ac.tuwien.sepm.assignment")
@PropertySource("classpath:application.properties")
public final class MainApplication extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    AnnotationConfigApplicationContext context;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // setup application
        primaryStage.setTitle("R.EPLAY");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(event -> {
            LOG.debug("Application shutdown initiated");
            if (context != null) {
                context.close();
            }
            Platform.exit();
            System.exit(0);
        });

        context = new AnnotationConfigApplicationContext("at.ac.tuwien.sepm.assignment.group");

        // prepare fxml loader to inject controller
        SpringFXMLLoader springFXMLLoader = context.getBean(SpringFXMLLoader.class);

        primaryStage.setScene(new Scene(springFXMLLoader.load("/fxml/mainwindow.fxml", Parent.class)));

        // show application
        primaryStage.show();
        primaryStage.toFront();
        LOG.debug("Application startup complete");
    }

    public static void main(String[] args) {
        LOG.debug("Application starting with arguments={}", (Object) args);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Application.launch(MainApplication.class, args);
    }

    private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        public void uncaughtException(Thread t, Throwable e) {
            LOG.warn("Unhandled exception caught!" ,e);
        }
    }

}
