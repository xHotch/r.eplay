package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Klampfl
 */
@Service
public class PlayerStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RigidBodyStatistic rigidBodyStatistic;
    private Map<Integer, List<RigidBodyInformation>> rigidBodyPlayers;

    public PlayerStatistic(RigidBodyStatistic rigidBodyStatistic) {
        this.rigidBodyStatistic = rigidBodyStatistic;
    }

    public void calculate(List<MatchPlayerDTO> matchPlayerDTOList) {
        LOG.trace("Called - calculate");
        Iterator<MatchPlayerDTO> matchPlayerDTOIterator = matchPlayerDTOList.iterator();
        for (int key : rigidBodyPlayers.keySet()) {
            rigidBodyStatistic.calculate(rigidBodyPlayers.get(key));
            MatchPlayerDTO matchPlayerDTO = matchPlayerDTOIterator.next();
            matchPlayerDTO.setAirTime(rigidBodyStatistic.getAirTime());
            matchPlayerDTO.setGroundTime(rigidBodyStatistic.getGroundTime());
            matchPlayerDTO.setAverageSpeed(rigidBodyStatistic.getAverageSpeed());
            if (matchPlayerDTO.getTeam() == TeamSide.RED) { //TODO check if red or blue side is on the positive side
                matchPlayerDTO.setEnemySideTime(rigidBodyStatistic.getNegativeSideTime());
                matchPlayerDTO.setHomeSideTime(rigidBodyStatistic.getPositiveSideTime());
            } else {
                matchPlayerDTO.setEnemySideTime(rigidBodyStatistic.getPositiveSideTime());
                matchPlayerDTO.setHomeSideTime(rigidBodyStatistic.getNegativeSideTime());
            }
        }
    }

    public void setRigidBodyPlayers(Map<Integer, List<RigidBodyInformation>> rigidBodyPlayers) {
        this.rigidBodyPlayers = rigidBodyPlayers;
    }
}
