package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.SortedMap;

/**
 * @author Markus Kogelbauer
 */
@Service
public class BallStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RigidBodyStatistic rigidBodyStatistic;

    private double possessionRed;
    private double possessionBlue;

    public BallStatistic(RigidBodyStatistic rigidBodyStatistic) {
        this.rigidBodyStatistic = rigidBodyStatistic;
    }

    public void calculate(MatchDTO match, List<RigidBodyInformation> rigidBodyBall, SortedMap<Double, TeamSide> hitTimes) {
        LOG.trace("Called - calculate");
        rigidBodyStatistic.calculate(rigidBodyBall);
        match.setBallHeatmapImage(rigidBodyStatistic.calculateHeatmap(rigidBodyBall));
        match.setTimeBallInBlueSide(rigidBodyStatistic.getNegativeSideTime());
        match.setTimeBallInRedSide(rigidBodyStatistic.getPositiveSideTime());
        calculatePossession(match, rigidBodyBall, hitTimes);
    }

    private void calculatePossession(MatchDTO match, List<RigidBodyInformation> rigidBodyBall, SortedMap<Double, TeamSide> hitTimes) {
        possessionRed = 0.0;
        possessionBlue = 0.0;
        TeamSide inPossession = null;
        double timeSince = 0.0;
        for (RigidBodyInformation rbi : rigidBodyBall) {
            if (hitTimes.containsKey(rbi.getFrameTime())) {
                addTime(inPossession,rbi.getFrameTime(), timeSince);
                inPossession = hitTimes.get(rbi.getFrameTime());
                timeSince = rbi.getFrameTime();
            }
            if (rbi.isGamePaused()) {
                if (inPossession != null) {
                    addTime(inPossession,rbi.getFrameTime(), timeSince);
                }
                inPossession = null;
            }
        }
        LOG.debug("time possession blue: {}, red: {}", possessionBlue, possessionRed);
        double total = possessionBlue + possessionRed;
        int blue = 0;
        int red = 0;
        if (total > 0.0) {
            blue = (int) ((100 / total) * possessionBlue);
            red = 100 - blue;
        }
        LOG.debug("percent blue: {}, red: {}", blue, red);
        match.setPossessionBlue(blue);
        match.setPossessionRed(red);
    }

    private void addTime(TeamSide inPossession, double timeTo, double timeFrom) {
        if (inPossession == TeamSide.BLUE) {
            possessionBlue += (timeTo - timeFrom);
        } else if (inPossession == TeamSide.RED) {
            possessionRed += (timeTo - timeFrom);
        }
    }
}
