package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import com.jayway.jsonpath.ReadContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GameInformationParser {

    private ReadContext ctx;

    private ArrayList<Double> timeOfGoals;

    /**
     * reads the time of each goal out of the json file and sets it in the timeOfGoals list
     *
     */
    void setTimeOfGoals() {
        timeOfGoals = new ArrayList<>();

        int numberOfGoals = ctx.read("$.TickMarks.length()");

        Double goalTime;
        //saves are stored as well in the TickMarks array, so eventType makes sure only goals are returned
        String eventType;

        for (int i = 0; i < numberOfGoals; i++) {

            eventType = ctx.read("$.TickMarks[" + i + "].Type", String.class);
            if (eventType.equals("Team0Goal") || eventType.equals("Team1Goal")) {
                goalTime = ctx.read("$.TickMarks[" + i + "].Time", Double.class);
                timeOfGoals.add(goalTime);
            }
        }

    }

    /**
     * checks if the game should be paused by checking if a goal was scored
     *
     * @param frameTime   current frame time
     * @return true if game should be paused
     * false if game should not be paused
     */
    boolean pauseGameIfGoalWasScored(Double frameTime) {
        if (!timeOfGoals.isEmpty()) {
            for (Double goalTime : timeOfGoals) {
                if (Double.compare(goalTime, frameTime) == 0) {
                    return true;
                }
            }

        }
        return false;
    }


    boolean resumeGameIfCountdownIsZero(String frame, int currentActorUpdateNr) {
        Integer countdown = ctx.read(frame + ".ActorUpdates[" + currentActorUpdateNr + "].['TAGame.GameEvent_TA:ReplicatedRoundCountDownNumber']", Integer.class);

        return (countdown == null) || (countdown != 0);
    }

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }
}
