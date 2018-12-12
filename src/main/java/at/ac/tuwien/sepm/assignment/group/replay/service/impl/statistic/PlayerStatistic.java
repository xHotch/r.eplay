package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import org.springframework.stereotype.Service;

/**
 * @author Daniel Klampfl
 */
@Service
public class PlayerStatistic {

    private RigidBodyStatistic rigidBodyStatistic;

    public PlayerStatistic(RigidBodyStatistic rigidBodyStatistic) {
        this.rigidBodyStatistic = rigidBodyStatistic;
    }


}
