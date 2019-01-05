package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.*;

import static java.lang.StrictMath.acos;

/**
 * Match Animation Controller.
 *
 * @author Philipp Hochhauser
 */
@Component
public class MatchAnimationController {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private MatchDTO matchDTO;
    private JsonParseService jsonParseService;
    private VideoDTO videoDTO;

    private HashMap<Rectangle, Integer> carShapes = new HashMap<>();
    private HashMap<Integer, RigidBodyInformation> rigidBodyInformationHashMap;
    private RigidBodyInformation ballInformation;

    private final int fieldWidth = 8192;
    private final int fieldLength = 10240;

    private final float scaleFactor = 0.075f;

    @FXML
    private Circle shape_ball;
    @FXML
    private Rectangle shape_car_blue_1;
    @FXML
    private Rectangle shape_car_blue_2;
    @FXML
    private Rectangle shape_car_blue_3;
    @FXML
    private Rectangle shape_car_red_1;
    @FXML
    private Rectangle shape_car_red_2;
    @FXML
    private Rectangle shape_car_red_3;


    @FXML
    private AnchorPane ap_MatchAnimation;

    @FXML
    private Canvas canvas_Animation;


    public MatchDTO getMatchDTO() {
        return matchDTO;
    }

    public void setMatchDTO(MatchDTO matchDTO) {
        this.matchDTO = matchDTO;
    }

    public MatchAnimationController(JsonParseService jsonParseService) {
        this.jsonParseService = jsonParseService;
    }

    public void onLoadAnimationButtonClicked(ActionEvent actionEvent){

        setupAnimation();


        final Timeline timeline = new Timeline();
        timeline.setAutoReverse(true);


        for (FrameDTO frameDTO : videoDTO.getFrames()){

            //List of Keyvalues
            List<KeyValue> values = new LinkedList<>();

            ballInformation = frameDTO.getBallRigidBodyInformation();
            values.addAll(mapBallToKayValue(shape_ball));

            rigidBodyInformationHashMap = frameDTO.getCarRigidBodyInformations();
            for (Rectangle car : carShapes.keySet()){
                values.addAll(mapCarToKayValue(car));
            }
            KeyFrame kf = new KeyFrame(Duration.seconds(frameDTO.getFrameTime()), "swag",null, values);
            timeline.getKeyFrames().add(kf);
        }

        timeline.play();
    }


    /**
     * Maps the Car's Rigidbodyinformation of the current frame onto the given Shape
     * Does not generate Keyvalues if there is no information about position / rotation
     *
     * @param carShape the shape
     * @return The List of Keyvalues, containing a Value for the X and Y Position and Keyvalue containing the Rotation
     */
    //todo fix rotation
    private List<KeyValue> mapCarToKayValue(Rectangle carShape){
        LinkedList<KeyValue> keyValues = new LinkedList<>();
        Vector3D position;
        Quaternion rotation;
        int actorId = carShapes.get(carShape);
        if (rigidBodyInformationHashMap.get(actorId)!=null) {
            if ((rigidBodyInformationHashMap.get(actorId).getPosition()) != null) {
                position = rigidBodyInformationHashMap.get(actorId).getPosition();
                KeyValue width = new KeyValue(carShape.xProperty(), position.getY() * scaleFactor);
                KeyValue length = new KeyValue(carShape.yProperty(), position.getX() * scaleFactor);
                keyValues.add(width);
                keyValues.add(length);
            }

            if (rigidBodyInformationHashMap.get(actorId).getRotation() != null) {
                rotation=rigidBodyInformationHashMap.get(actorId).getRotation();
                double ang = 2*acos(rotation.getQ0());
                KeyValue rotationValue = new KeyValue(carShape.rotateProperty(), ang*180/Math.PI );
                keyValues.add(rotationValue);
            }

        }
        return keyValues;
    }


    /**
     * Maps the Ball's Rigidbodyinformation of the current frame onto the given Shape
     * Does not generate Keyvalues if there is no information about position
     *
     * @param ballShape the shape
     * @return The List of Keyvalues, containing a Value for the X and Y Position
     */
    private List<KeyValue> mapBallToKayValue(Circle ballShape){
        LinkedList<KeyValue> keyValues = new LinkedList<>();
        if (ballInformation!=null && ballInformation.getPosition()!= null) {
            KeyValue width = new KeyValue(ballShape.centerXProperty(), ballInformation.getPosition().getY() * scaleFactor);
            KeyValue length = new KeyValue(ballShape.centerYProperty(), ballInformation.getPosition().getX() * scaleFactor);
            keyValues.add(width);
            keyValues.add(length);
        }
        return keyValues;
    }


    /**
     * Sets up the Animation. Spawns a Car for each player
     * Parses VideoDTO from the replay file
     */
    private void setupAnimation(){
        try {
            videoDTO = jsonParseService.getVideo(matchDTO);
        } catch (FileServiceException e){
            //todo
        }

        Map<Long, Integer> actorToPlatformId = videoDTO.getActorIds();

        int countRed = 0;
        int countBlue = 0;
        for (MatchPlayerDTO player : matchDTO.getPlayerData()){
            Integer actorId = actorToPlatformId.get(player.getPlayerDTO().getPlatformID());
            if (player.getTeam() == TeamSide.RED) {
                if(countRed == 0) {
                    carShapes.put(shape_car_red_1, actorId);
                    shape_car_red_1.setVisible(true);
                } else if (countRed == 1) {
                    carShapes.put(shape_car_red_2, actorId);
                    shape_car_red_2.setVisible(true);
                } else if (countRed == 2) {
                    carShapes.put(shape_car_red_3, actorId);
                    shape_car_red_3.setVisible(true);
                }
                countRed ++;
            } else {
                if(countBlue == 0) {
                    carShapes.put(shape_car_blue_1, actorId);
                    shape_car_blue_1.setVisible(true);
                } else if (countBlue == 1) {
                    carShapes.put(shape_car_blue_2, actorId);
                    shape_car_blue_2.setVisible(true);
                } else if (countBlue == 2) {
                    carShapes.put(shape_car_blue_3, actorId);
                    shape_car_blue_3.setVisible(true);
                }
                countBlue ++;
            }
        }
    }

}
