package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.TeamSide;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Klampfl
 */
@Service
public class PlayerStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private RigidBodyStatistic rigidBodyStatistic;

    public PlayerStatistic(RigidBodyStatistic rigidBodyStatistic) {
        this.rigidBodyStatistic = rigidBodyStatistic;
    }

    public void calculate(List<MatchPlayerDTO> matchPlayerDTOList,Map<Integer,List<RigidBodyInformation>> rigidBodyPlayers, List<RigidBodyInformation> rigidBodyBall) {
        LOG.trace("Called - calculate");
        matchPlayerDTOList.forEach(dto -> setMatchPlayerData(dto, rigidBodyPlayers.get(dto.getActorId()), rigidBodyBall));
    }

    private void setMatchPlayerData(MatchPlayerDTO matchPlayerDTO, List<RigidBodyInformation> rigidBodyPlayer, List<RigidBodyInformation> rigidBodyBall){
        rigidBodyStatistic.calculate(rigidBodyPlayer);
        matchPlayerDTO.setHeatmapImage(rigidBodyStatistic.calculateHeatmap(rigidBodyPlayer));
        matchPlayerDTO.setAirTime(rigidBodyStatistic.getAirTime());
        matchPlayerDTO.setGroundTime(rigidBodyStatistic.getGroundTime());
        matchPlayerDTO.setAverageSpeed(rigidBodyStatistic.getAverageSpeed());
        matchPlayerDTO.setAverageDistanceToBall(rigidBodyStatistic.averageDistanceTo(rigidBodyPlayer, rigidBodyBall));
        if (matchPlayerDTO.getTeam() == TeamSide.RED) {
            matchPlayerDTO.setHomeSideTime(rigidBodyStatistic.getPositiveSideTime());
            matchPlayerDTO.setEnemySideTime(rigidBodyStatistic.getNegativeSideTime());
        } else {
            matchPlayerDTO.setHomeSideTime(rigidBodyStatistic.getNegativeSideTime());
            matchPlayerDTO.setEnemySideTime(rigidBodyStatistic.getPositiveSideTime());
        }
    }

}
