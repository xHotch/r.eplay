package at.ac.tuwien.sepm.assignment.group.replay.ui;

import at.ac.tuwien.sepm.assignment.group.replay.dto.*;
import at.ac.tuwien.sepm.assignment.group.replay.service.JsonParseService;
import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser.BoostInformationParser;
import at.ac.tuwien.sepm.assignment.group.util.AlertHelper;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ExecutorService;
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
    private ExecutorService executorService;

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
    private ImageView player1boost;
    @FXML
    private ImageView player2boost;
    @FXML
    private ImageView player3boost;
    @FXML
    private ImageView player4boost;
    @FXML
    private ImageView player5boost;
    @FXML
    private ImageView player6boost;
    @FXML
    private Label player1boost_label;
    @FXML
    private Label player2boost_label;
    @FXML
    private Label player3boost_label;
    @FXML
    private Label player4boost_label;
    @FXML
    private Label player5boost_label;
    @FXML
    private Label player6boost_label;

    private final Timeline timeline = new Timeline();

    public MatchAnimationController(JsonParseService jsonParseService, BoostInformationParser boostInformationParser, ExecutorService executorService) {
        this.jsonParseService = jsonParseService;
        this.executorService = executorService;
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

    /**
     * Method that sets up the Animation in a separate thread and starts the animation Playback afterwards
     */
    @FXML
    private void onLoadAnimationButtonClicked(){
        executorService.submit(() -> {

            try {
                setupAnimation();
            } catch (FileServiceException e){
                LOG.error("Caught FileServiceException trying to setup Animation", e);
                AlertHelper.showErrorMessage("Animation konnte nicht geladen werden");
                return;
            }

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
                Platform.runLater(() -> {
                    playAnimation();
                });
            });
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

    void pauseAnimation() {
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
    private void setupAnimation() throws FileServiceException{

        videoDTO = jsonParseService.getVideo(matchDTO);


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

    private void generateBoostTimeline(double imagelength){

        Map<Integer, List<BoostDTO>> boostAmount = boostInformationParser.getBoostAmountMap();

        Map<Long, Integer> actorToPlatformId = videoDTO.getActorIds();

        int countBlue = 0;
        int countRed = 0;
        boolean isTeamBluePlayer1 = false;
        boolean isTeamBluePlayer2 = false;
        boolean isTeamBluePlayer3 = false;
        boolean isTeamRedPlayer1 = false;
        boolean isTeamRedPlayer2 = false;
        boolean isTeamRedPlayer3 = false;

        for (MatchPlayerDTO player : matchDTO.getPlayerData()){

            BufferedImage boostPlayer = new BufferedImage((int)imagelength,1,BufferedImage.TYPE_INT_ARGB);

            Color white = new Color(255, 255, 255); // Color White
            Color teamBluePlayer1 = new Color(0, 36, 255);
            Color teamBluePlayer2 = new Color(192, 0, 255);
            Color teamBluePlayer3 = new Color(22, 188, 0);
            Color teamRedPlayer1 = new Color(255, 0, 0);
            Color teamRedPlayer2 = new Color(255, 204, 0);
            Color teamRedPlayer3 = new Color(255, 255, 0);

            Integer actorId = actorToPlatformId.get(player.getPlayerDTO().getPlatformID());

            Color currentColor = new Color(255, 255, 255);

            //Select Color for Player
            if (player.getTeam() == TeamSide.BLUE) {
                if(countBlue == 0) {
                    currentColor = teamBluePlayer1;
                    isTeamBluePlayer1 = true;
                } else if (countBlue == 1) {
                    currentColor = teamBluePlayer2;
                    isTeamBluePlayer2 = true;
                } else if (countBlue == 2) {
                    currentColor = teamBluePlayer3;
                    isTeamBluePlayer3 = true;
                }
                countBlue ++;
            } else {
                if(countRed == 0) {
                    currentColor = teamRedPlayer1;
                    isTeamRedPlayer1 = true;
                } else if (countRed == 1) {
                    currentColor = teamRedPlayer2;
                    isTeamRedPlayer2 = true;
                } else if (countRed == 2) {
                    currentColor = teamRedPlayer3;
                    isTeamRedPlayer3 = true;
                }
                countRed ++;
            }


            if(boostAmount.containsKey(actorId)){
                List<BoostDTO> boost = boostAmount.get(actorId);

                int currentFrameTime = 0;
                int lastFrameTime = 0;
                Color color;

                for (BoostDTO entry : boost){
                    currentFrameTime = (int) Math.floor(entry.getFrameTime());

                    //Fill from beginning to first boost frameTime without any opacity
                    if(lastFrameTime == 0){
                        color = mixColorsWithAlpha(white, currentColor, 255);
                        for(int i = lastFrameTime; i < currentFrameTime; i++){
                            boostPlayer.setRGB(i, 0, color.getRGB());
                        }
                    }

                    //Calculate new color with boost as opacity
                    int boostValue = entry.getBoostAmount();
                    int opacity = scaleBoostOpacity(boostValue,0,100,0,255);
                    color = mixColorsWithAlpha(white, currentColor, opacity);
                    for(int i = currentFrameTime; i < boostPlayer.getWidth(); i++){
                        boostPlayer.setRGB(i, 0, color.getRGB());
                    }

                    lastFrameTime = currentFrameTime;
                }
            }

            //Insert Bufferedimage into imageview item
            if(isTeamBluePlayer1) {
                player1boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player1boost.setScaleY(player1boost.getFitHeight());
                player1boost_label.setText(player.getName());
                isTeamBluePlayer1 = false;
            } else if(isTeamBluePlayer2) {
                player2boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player2boost.setScaleY(player2boost.getFitHeight());
                player2boost_label.setText(player.getName());
                isTeamBluePlayer2 = false;
            } else if(isTeamBluePlayer3) {
                player3boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player3boost.setScaleY(player3boost.getFitHeight());
                player3boost_label.setText(player.getName());
                isTeamBluePlayer3 = false;
            } else if(isTeamRedPlayer1) {
                player4boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player4boost.setScaleY(player4boost.getFitHeight());
                player4boost_label.setText(player.getName());
                isTeamRedPlayer1 = false;
            } else if(isTeamRedPlayer2) {
                player5boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player5boost.setScaleY(player5boost.getFitHeight());
                player5boost_label.setText(player.getName());
                isTeamRedPlayer2 = false;
            } else if(isTeamRedPlayer3) {
                player6boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                player6boost.setScaleY(player6boost.getFitHeight());
                player6boost_label.setText(player.getName());
                isTeamRedPlayer3 = false;
            }

        }

    }

    private Color mixColorsWithAlpha(Color color1, Color color2, int alpha)
    {
        float factor = alpha / 255f;
        int red = (int) (color1.getRed() * (1 - factor) + color2.getRed() * factor);
        int green = (int) (color1.getGreen() * (1 - factor) + color2.getGreen() * factor);
        int blue = (int) (color1.getBlue() * (1 - factor) + color2.getBlue() * factor);
        return new Color(red, green, blue);
    }

    private int scaleBoostOpacity(int value, int oldMin, int oldMax, int newMin, int newMax){
        return (((newMax-newMin)*(value-oldMin))/(oldMax-oldMin))+newMin;
    }

    public MatchDTO getMatchDTO() {
        return matchDTO;
    }

    public void setMatchDTO(MatchDTO matchDTO) {
        this.matchDTO = matchDTO;
    }

}
