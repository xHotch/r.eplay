package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser.BoostInformationParser;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.List;

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
    private BoostInformationParser boostInformationParser;

    private HashMap<Rectangle, Integer> carShapes = new HashMap<>();
    private HashMap<Integer, RigidBodyInformation> rigidBodyInformationHashMap;
    private RigidBodyInformation ballInformation;

    private final int fieldWidth = 8192;
    private final int fieldLength = 10240;

    private final float scaleFactor = 0.075f;
    private static double MIN_SLIDER_CHANGE = 0.5;

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
    private Slider timelineSlider;
    @FXML
    private ImageView playPauseImageView;

    private Image pauseImage;
    private Image playImage;

    @FXML
    private AnchorPane ap_MatchAnimation;

    @FXML
    private Canvas canvas_Animation;

    private Boolean play = false;
    private Boolean stopped = false;

    @FXML
    ImageView player1boost;

    @FXML
    ImageView player2boost;

    @FXML
    ImageView player3boost;

    @FXML
    ImageView player4boost;

    @FXML
    ImageView player5boost;

    @FXML
    ImageView player6boost;

    private final Timeline timeline = new Timeline();

    public MatchAnimationController(JsonParseService jsonParseService, BoostInformationParser boostInformationParser) {
        this.jsonParseService = jsonParseService;
        this.boostInformationParser = boostInformationParser;
    }

    @FXML
    private void initialize() {
        pauseImage = new Image("images/pause.gif");
        playImage = new Image("images/play.gif");
        //Listener to change animation point if slider value change is > MIN_SLIDER_CHANGE (mouse click)
        timelineSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (!timelineSlider.isValueChanging()) {
                double currentTime = timeline.getCurrentTime().toSeconds();
                double sliderTime = newValue.doubleValue();
                if (Math.abs(currentTime - sliderTime) > MIN_SLIDER_CHANGE) {
                    timeline.jumpTo(Duration.seconds(newValue.doubleValue()));
                }
            }
        });
        //Jump to animation frame time from slider position after mouse drag
        timelineSlider.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            if(!isNowChanging) {
                timeline.jumpTo(Duration.seconds(timelineSlider.getValue()));
            }
        });
        //Change Slider Position to match animation
        timeline.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!timelineSlider.isValueChanging() ) timelineSlider.setValue(newTime.toSeconds());
        });

    }

    @FXML
    private void onLoadAnimationButtonClicked(){

        setupAnimation();

        timeline.setAutoReverse(true);
        timeline.getKeyFrames().clear();

        //animation data
        double minFrameTime = Double.MIN_VALUE;
        double maxFrameTime = 0;
        for (FrameDTO frameDTO : videoDTO.getFrames()){

            //List of Keyvalues
            List<KeyValue> values = new LinkedList<>();

            ballInformation = frameDTO.getBallRigidBodyInformation();
            values.addAll(mapBallToKayValue(shape_ball));

            rigidBodyInformationHashMap = frameDTO.getCarRigidBodyInformations();
            for (Rectangle car : carShapes.keySet()){
                values.addAll(mapCarToKayValue(car));
            }
            double frameTime = frameDTO.getFrameTime();
            KeyFrame kf = new KeyFrame(Duration.seconds(frameTime), "swag", null, values);
            timeline.getKeyFrames().add(kf);
            if (minFrameTime > frameTime) minFrameTime = frameTime;
            if (maxFrameTime < frameTime) maxFrameTime = frameTime;
        }

        generateBoostTimeline(maxFrameTime);

        //slider settings
        timelineSlider.setMin(minFrameTime);
        timelineSlider.setMax(maxFrameTime);

        stopped = true;
        playAnimation();
    }

    @FXML
    private void onStopButtonClicked() {
        LOG.info("Stop button Clicked");
        stopped = true;
        timeline.stop();
        playPauseImageView.setImage(playImage);
        play = false;
    }

    @FXML
    private void onPlayPauseButtonClicked() {
        LOG.info("Play/Pause Button Clicked");
        if (play) {
            pauseAnimation();
        } else {
            playAnimation();
        }
    }

    private void playAnimation() {
        play = true;
        if(stopped) {
            stopped = false;
            timeline.playFromStart();
        }
        else timeline.play();
        playPauseImageView.setImage(pauseImage);
    }

    private void pauseAnimation() {
        play = false;
        timeline.pause();
        playPauseImageView.setImage(playImage);
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

            /*if (rigidBodyInformationHashMap.get(actorId).getRotation() != null) {
                rotation=rigidBodyInformationHashMap.get(actorId).getRotation();
                double ang = 2*acos(rotation.getQ0());
                KeyValue rotationValue = new KeyValue(carShape.rotateProperty(), ang*180/Math.PI );
                keyValues.add(rotationValue);
            }*/

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

    private BufferedImage generateBoostTimeline(double imagelength){

        BufferedImage boostPlayer = new BufferedImage((int)imagelength,1,BufferedImage.TYPE_INT_RGB);

        Color white = new Color(255, 255, 255); // Color White
        int rgbWhite = white.getRGB();

        for(int i = 0; i < boostPlayer.getWidth(); i++){
            boostPlayer.setRGB(i, 0, rgbWhite);
        }

        Map<Integer, List<BoostDTO>> boostAmount = boostInformationParser.getBoostAmountMap();
        Map<Integer, Integer> carBoostMap = boostInformationParser.getCarBoostMap();

        //for (FrameDTO frameDTO : videoDTO.getFrames()){

        //}

        Map<Long, Integer> actorToPlatformId = videoDTO.getActorIds();

        for(Map.Entry<Integer, List<BoostDTO>> boostPadInfo:boostAmount.entrySet()){

        }

        Color myColor = new Color(255, 0, 0); // Color Red
        int rgb = myColor.getRGB();

        int playercount = 1;

        for (MatchPlayerDTO player : matchDTO.getPlayerData()){
            Integer actorId = actorToPlatformId.get(player.getPlayerDTO().getPlatformID());
            //long pid = player.getPlayerDTO().getPlatformID();
            //Integer id = player.getActorId();
            //int i = 1;

            List<Integer> carIDforActor = new LinkedList<Integer>();

            for (Map.Entry<Integer, Integer> entry : carBoostMap.entrySet()) {
                if (entry.getValue().equals(actorId)){
                    carIDforActor.add(entry.getKey());
                }
            }

            for (Integer carID : carIDforActor){
                if(boostAmount.containsKey(carID)){
                    List<BoostDTO> boost = boostAmount.get(carID);

                    for (BoostDTO entry : boost){
                        boostPlayer.setRGB((int)Math.floor(entry.getFrameTime()), 0, rgb);
                    }
                }
            }

//            File outputfile = new File("image.bmp");
//            try {
//                ImageIO.write(boostPlayer, "bmp", outputfile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            if(playercount == 1) {
                player1boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                //player1boost.setRotate(270);
                //player1boost.setScaleX(1.5);
                player1boost.setScaleY(8);
            } else if(playercount == 2) {
                player2boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player2boost.setScaleY(8);
            } else if(playercount == 3) {
                player3boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player3boost.setScaleY(8);
            } else if(playercount == 4) {
                player4boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player4boost.setScaleY(8);
            } else if(playercount == 5) {
                player5boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player5boost.setScaleY(8);
            } else if(playercount == 6) {
                player6boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player6boost.setScaleY(8);
            }

            playercount++;

        }

        return boostPlayer;
    }

    public MatchDTO getMatchDTO() {
        return matchDTO;
    }

    public void setMatchDTO(MatchDTO matchDTO) {
        this.matchDTO = matchDTO;
    }

}
