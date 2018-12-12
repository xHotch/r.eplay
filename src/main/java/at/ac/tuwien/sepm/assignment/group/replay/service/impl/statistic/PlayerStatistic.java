package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Klampfl
 */
@Service
public class PlayerStatistic {

    private RigidBodyStatistic rigidBodyStatistic;
    private Map<Integer,List<RigidBodyInformation>> rigidBodyPlayers;

    public PlayerStatistic(RigidBodyStatistic rigidBodyStatistic) {
        this.rigidBodyStatistic = rigidBodyStatistic;
    }

    public void calculate()
    {
        for (int key : rigidBodyPlayers.keySet()) {
            rigidBodyStatistic.calculate(rigidBodyPlayers.get(key));
        }
        //TODO save statistics in MatchPlayerDTO
    }

    public void setRigidBodyPlayers(Map<Integer, List<RigidBodyInformation>> rigidBodyPlayers) {
        this.rigidBodyPlayers = rigidBodyPlayers;
    }
}
