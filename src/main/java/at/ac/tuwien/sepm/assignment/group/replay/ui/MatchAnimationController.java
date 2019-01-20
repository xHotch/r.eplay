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
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.awt.image.BufferedImage;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.List;

import static javafx.scene.paint.Color.color;

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
    private Map<Integer, Integer> carToPlayerMap;
    private MultiValueMap<Integer, Pair<Integer, Double>> playerToCarAndTimeMap;
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

    private Color white = Color.rgb(255, 255, 255); // Color White

    private Color teamBluePlayer1 = Color.rgb(0, 153, 255);
    private Color teamBluePlayer2 = Color.rgb(0, 0, 255);
    private Color teamBluePlayer3 = Color.rgb(0, 0, 90);

    private Color teamRedPlayer1 = Color.rgb(255, 102, 204);
    private Color teamRedPlayer2 = Color.rgb(255, 0, 0);
    private Color teamRedPlayer3 = Color.rgb(80, 0, 0);


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
            if (!isNowChanging) {
                timeline.jumpTo(Duration.seconds(timelineSlider.getValue()));
            }
        });
        //Change Slider Position to match animation
        timeline.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!timelineSlider.isValueChanging()) timelineSlider.setValue(newTime.toSeconds());
        });

    }

    /**
     * Method that sets up the Animation in a separate thread and starts the animation Playback afterwards
     */
    @FXML
    private void onLoadAnimationButtonClicked() {
        executorService.submit(() -> {

            try {
                setupAnimation();
            } catch (FileServiceException e) {
                LOG.error("Caught FileServiceException trying to setup Animation", e);
                AlertHelper.showErrorMessage("Animation konnte nicht geladen werden");
                return;
            }

            timeline.setAutoReverse(true);
            timeline.getKeyFrames().clear();

            //animation data
            double minFrameTime = Double.MIN_VALUE;
            double maxFrameTime = 0;

            for (FrameDTO frameDTO : videoDTO.getFrames()) {

                //List of Keyvalues
                List<KeyValue> values = new LinkedList<>();

                ballInformation = frameDTO.getBallRigidBodyInformation();
                values.addAll(mapBallToKayValue(shape_ball));

                rigidBodyInformationHashMap = frameDTO.getCarRigidBodyInformations();
                for (Rectangle car : carShapes.keySet()) {
                    values.addAll(mapCarToKayValue(car, frameDTO.getFrameTime()));
                }
                double frameTime = frameDTO.getFrameTime();
                KeyFrame kf = new KeyFrame(Duration.seconds(frameTime),"",null, values);
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
        if (stopped) {
            stopped = false;
            timeline.playFromStart();
        } else timeline.play();
        playPauseImageView.setImage(pauseImage);
    }

    void pauseAnimation() {
        play = false;
        timeline.pause();
        playPauseImageView.setImage(playImage);
    }

    /**
     * Maps the Car's Rigidbodyinformation of the current frame onto the given Shape.
     * Has to find the right CarActorID for the given FrameTime
     * Does not generate Keyvalues if there is no information about position
     *
     * @param carShape the shape
     * @return The List of Keyvalues, containing a Value for the X and Y Position
     */
    private List<KeyValue> mapCarToKayValue(Rectangle carShape, double frameTime) {
        LinkedList<KeyValue> keyValues = new LinkedList<>();
        Vector3D position;
        int carActorId = carShapes.get(carShape);


        int actorId = -1;
        double oldFrameTime = 0.0f;

        for (Pair<Integer, Double> carAndFrameTime : playerToCarAndTimeMap.get(carActorId)) {
            double carFrameTime = carAndFrameTime.getValue();
            if (carFrameTime <= frameTime) {
                if (oldFrameTime < carFrameTime)
                    actorId = carAndFrameTime.getKey();
                oldFrameTime = carAndFrameTime.getValue();
            }
        }


        if (rigidBodyInformationHashMap.get(actorId) != null) {
            if ((rigidBodyInformationHashMap.get(actorId).getPosition()) != null) {
                position = rigidBodyInformationHashMap.get(actorId).getPosition();
                KeyValue width = new KeyValue(carShape.xProperty(), position.getY() * scaleFactor);
                KeyValue length = new KeyValue(carShape.yProperty(), position.getX() * scaleFactor);
                keyValues.add(width);
                keyValues.add(length);
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
    private List<KeyValue> mapBallToKayValue(Circle ballShape) {
        LinkedList<KeyValue> keyValues = new LinkedList<>();
        if (ballInformation != null && ballInformation.getPosition() != null) {
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
    private void setupAnimation() throws FileServiceException {

        videoDTO = jsonParseService.getVideo(matchDTO);


        Map<Long, Integer> actorToPlatformId = videoDTO.getActorIds();
        carToPlayerMap = videoDTO.getCarActorIds();
        playerToCarAndTimeMap = videoDTO.getPlayerToCarAndTimeMap();

        int countRed = 0;
        int countBlue = 0;
        for (MatchPlayerDTO player : matchDTO.getPlayerData()) {
            Integer actorId = actorToPlatformId.get(player.getPlayerDTO().getPlatformID());
            if (player.getTeam() == TeamSide.RED) {
                if (countRed == 0) {
                    carShapes.put(shape_car_red_1, actorId);
                    shape_car_red_1.setVisible(true);
                    shape_car_red_1.setFill(teamRedPlayer1);
                } else if (countRed == 1) {
                    carShapes.put(shape_car_red_2, actorId);
                    shape_car_red_2.setVisible(true);
                    shape_car_red_2.setFill(teamRedPlayer2);
                } else if (countRed == 2) {
                    carShapes.put(shape_car_red_3, actorId);
                    shape_car_red_3.setVisible(true);
                    shape_car_red_3.setFill(teamRedPlayer3);
                }
                countRed++;
            } else {
                if (countBlue == 0) {
                    carShapes.put(shape_car_blue_1, actorId);
                    shape_car_blue_1.setVisible(true);
                    shape_car_blue_1.setFill(teamBluePlayer1);
                } else if (countBlue == 1) {
                    carShapes.put(shape_car_blue_2, actorId);
                    shape_car_blue_2.setFill(teamBluePlayer2);
                    shape_car_blue_2.setVisible(true);
                } else if (countBlue == 2) {
                    carShapes.put(shape_car_blue_3, actorId);
                    shape_car_blue_3.setVisible(true);
                    shape_car_blue_3.setFill(teamBluePlayer3);
                }
                countBlue++;
            }
        }
    }

    private void generateBoostTimeline(double imagelength) {

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

        for (MatchPlayerDTO player : matchDTO.getPlayerData()) {

            BufferedImage boostPlayer = new BufferedImage((int) imagelength, 1, BufferedImage.TYPE_INT_ARGB);


            Integer actorId = actorToPlatformId.get(player.getPlayerDTO().getPlatformID());

            Color currentColor = Color.rgb(255, 255, 255);




            //Select Color for Player
            if (player.getTeam() == TeamSide.BLUE) {
                if (countBlue == 0) {
                    isTeamBluePlayer1 = true;
                } else if (countBlue == 1) {
                    isTeamBluePlayer2 = true;
                } else if (countBlue == 2) {
                    isTeamBluePlayer3 = true;
                }
                countBlue++;
            } else {
                if (countRed == 0) {
                    isTeamRedPlayer1 = true;
                } else if (countRed == 1) {
                    isTeamRedPlayer2 = true;
                } else if (countRed == 2) {
                    isTeamRedPlayer3 = true;
                }
                countRed++;
            }

            for(Map.Entry<Rectangle, Integer> carShape : carShapes.entrySet()){
                if (carShape.getValue() == actorId){
                    currentColor = (Color)carShape.getKey().getFill();
                }
            }


            if (boostAmount.containsKey(actorId)) {
                List<BoostDTO> boost = boostAmount.get(actorId);
                boost.sort(Comparator.comparing(BoostDTO::getFrameTime)); //Sort list by frameTime

                int currentFrameTime = 0;
                int lastFrameTime = 0;
                Color color;

                for (BoostDTO entry : boost) {
                    currentFrameTime = (int) Math.floor(entry.getFrameTime());

                    //Fill from beginning to first boost frameTime with 33 boost (a player starts with 1/3 boost amount)
                    if (lastFrameTime == 0) {
                        int opacity = scaleBoostOpacity(33, 0, 100, 0, 255);
                        color = mixColorsWithAlpha(white, currentColor, opacity);
                        for (int i = lastFrameTime; i < currentFrameTime; i++) {
                            boostPlayer.setRGB(i, 0, getIntFromColor(color));
                        }
                    }

                    //Calculate new color with boost as opacity
                    int boostValue = entry.getBoostAmount();
                    int opacity = scaleBoostOpacity(boostValue, 0, 100, 0, 255);
                    color = mixColorsWithAlpha(white, currentColor, opacity);
                    for (int i = currentFrameTime; i < boostPlayer.getWidth(); i++) {
                        boostPlayer.setRGB(i, 0, getIntFromColor(color));
                    }

                    lastFrameTime = currentFrameTime;
                }
            }

            //Insert Bufferedimage into imageview item
            if (isTeamBluePlayer1) {
                Platform.runLater(() -> {
                    player1boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                    player1boost.setScaleY(player1boost.getFitHeight());
                    player1boost_label.setText(player.getName());
                });
                isTeamBluePlayer1 = false;
            } else if (isTeamBluePlayer2) {
                Platform.runLater(() -> {
                    player2boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                    player2boost.setScaleY(player2boost.getFitHeight());
                    player2boost_label.setText(player.getName());
                });
                isTeamBluePlayer2 = false;
            } else if (isTeamBluePlayer3) {
                Platform.runLater(() -> {
                    player3boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                    player3boost.setScaleY(player3boost.getFitHeight());
                    player3boost_label.setText(player.getName());
                });
                isTeamBluePlayer3 = false;
            } else if (isTeamRedPlayer1) {
                Platform.runLater(() -> {
                    player4boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                    player4boost.setScaleY(player4boost.getFitHeight());
                    player4boost_label.setText(player.getName());
                });
                isTeamRedPlayer1 = false;
            } else if (isTeamRedPlayer2) {
                Platform.runLater(() -> {
                    player5boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                    player5boost.setScaleY(player5boost.getFitHeight());
                    player5boost_label.setText(player.getName());
                });
                isTeamRedPlayer2 = false;
            } else if (isTeamRedPlayer3) {
                Platform.runLater(() -> {
                    player6boost.setImage(SwingFXUtils.toFXImage(boostPlayer, null));
                    player6boost.setScaleY(player6boost.getFitHeight());
                    player6boost_label.setText(player.getName());
                });
                isTeamRedPlayer3 = false;
            }

        }

    }

    private Color mixColorsWithAlpha(Color color1, Color color2, int alpha) {
        double factor = alpha / 255f;
        double red = (color1.getRed() * (1 - factor) + color2.getRed() * factor);
        double green = (color1.getGreen() * (1 - factor) + color2.getGreen() * factor);
        double blue = (color1.getBlue() * (1 - factor) + color2.getBlue() * factor);
        return Color.color(red, green, blue);
    }

    private int scaleBoostOpacity(int value, int oldMin, int oldMax, int newMin, int newMax) {
        return (((newMax - newMin) * (value - oldMin)) / (oldMax - oldMin)) + newMin;
    }

    public MatchDTO getMatchDTO() {
        return matchDTO;
    }

    public void setMatchDTO(MatchDTO matchDTO) {
        this.matchDTO = matchDTO;
    }

    public int getIntFromColor(Color color) {
        int R = Math.round(255 * (float) color.getRed());
        int G = Math.round(255 * (float) color.getGreen());
        int B = Math.round(255 * (float) color.getBlue());

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

}
