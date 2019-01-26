package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * @author Elias Brugger
 */
@Service
public class BoostStatistic {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * assigns a the boost pads informtaions to the corresponding match players.
     * @param matchPlayerDTOList the list of all match players.
     * @param rigidBodyPlayers the list of all rigid body players to retrieve the actor ids for the boost pad map.
     * @param boostPadMap the map of all boost pad lists assigned to the corresponding actor id.
     */
    public void calculate(List<MatchPlayerDTO> matchPlayerDTOList, List<RigidBodyInformation> rigidBodyPlayers, Map<Integer, Map<Integer, List<BoostPadDTO>>> boostPadMap){
        LOG.trace("Called - calculate in BoostStatistic");
        matchPlayerDTOList.forEach(dto -> {
            LOG.debug("DEBUG actorID (Boost Statistic): {}", dto.getActorId());
            setMatchPlayerData(dto, boostPadMap.get(dto.getActorId()));
        });
    }

    /**
     * set the boost pad information of the match player
     * @param matchPlayerDTO the match player
     * @param boostPadDTOMap the boost pad map with the picked up amounts per boost pad from the specified player
     */
    public void setMatchPlayerData(MatchPlayerDTO matchPlayerDTO, Map<Integer, List<BoostPadDTO>> boostPadDTOMap){
        matchPlayerDTO.setBoostPadMap(boostPadDTOMap);
    }
}
