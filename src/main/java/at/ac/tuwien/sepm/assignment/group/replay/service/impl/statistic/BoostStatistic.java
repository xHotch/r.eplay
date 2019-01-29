package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.BoostPadDTO;
import at.ac.tuwien.sepm.assignment.group.replay.dto.MatchPlayerDTO;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
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
     *
     * @param matchPlayerDTOList the list of all match players.
     * @param rigidBodyPlayers   the list of all rigid body players to retrieve the actor ids for the boost pad map.
     * @param boostPadMap        the map of all boost pad lists assigned to the corresponding actor id.
     */
    public void calculate(List<MatchPlayerDTO> matchPlayerDTOList, List<RigidBodyInformation> rigidBodyPlayers, Map<Integer, Map<Integer, List<BoostPadDTO>>> boostPadMap, Map<Integer, List<BoostDTO>> boostAmountMap) {
        LOG.trace("Called - calculate in BoostStatistic");
        matchPlayerDTOList.forEach(dto -> {
            LOG.debug("DEBUG actorID (Boost Statistic): {}", dto.getActorId());
            setMatchPlayerData(dto, boostPadMap.get(dto.getActorId()));
            setBoostPerMinute(dto, boostAmountMap.get(dto.getActorId()));

        });
    }

    /**
     * calculates the Boost Amount statistics for a match player
     *
     * @param dto       the match player
     * @param boostDTOS the list of BoostDTOS holding the boostamount
     */
    private void setBoostPerMinute(MatchPlayerDTO dto, List<BoostDTO> boostDTOS) {
        int lowBoostThreshold = 20;
        int fullBoostThreshold = 80;
        LOG.trace("called - setBoostPerMinute");

        double totalBoost = 0.0;
        double timePaused = 0.0;
        double timeLowBoost = 0.0;
        double timeFullBoost = 0.0;

        boostDTOS.sort(Comparator.comparing(BoostDTO::getFrameTime));
        BoostDTO firstBoostDTO = boostDTOS.get(0);
        BoostDTO secondBoostDTO = boostDTOS.get(1);


        for (int i = 1; i < boostDTOS.size() - 1; i++) {
            if (secondBoostDTO.getBoostAmount() > firstBoostDTO.getBoostAmount()) {
                totalBoost += secondBoostDTO.getBoostAmount() - firstBoostDTO.getBoostAmount();
            }
            if (secondBoostDTO.isGamePaused()) {
                timePaused += secondBoostDTO.getFrameTime() - firstBoostDTO.getFrameTime();
            }

            if (firstBoostDTO.getBoostAmount() < lowBoostThreshold) {
                timeLowBoost += secondBoostDTO.getFrameTime() - firstBoostDTO.getFrameTime();
            } else if (firstBoostDTO.getBoostAmount() > fullBoostThreshold) {
                timeFullBoost += secondBoostDTO.getFrameTime() - firstBoostDTO.getFrameTime();
            }

            firstBoostDTO = boostDTOS.get(i);
            secondBoostDTO = boostDTOS.get(i + 1);

        }

        double time = boostDTOS.get(boostDTOS.size() - 1).getFrameTime() - boostDTOS.get(0).getFrameTime();
        
        dto.setBoostPerMinute(totalBoost / ((time - timePaused) / 60));
        dto.setTimeFullBoost(timeFullBoost);
        dto.setTimeLowBoost(timeLowBoost);
    }

    /**
     * set the boost pad information of the match player
     *
     * @param matchPlayerDTO the match player
     * @param boostPadDTOMap the boost pad map with the picked up amounts per boost pad from the specified player
     */
    private void setMatchPlayerData(MatchPlayerDTO matchPlayerDTO, Map<Integer, List<BoostPadDTO>> boostPadDTOMap) {
        matchPlayerDTO.setBoostPadMap(boostPadDTOMap);

        int amount = 0;
        for (Map.Entry<Integer, List<BoostPadDTO>> boostPadList : boostPadDTOMap.entrySet()) {
            amount += boostPadList.getValue().size();
        }
        matchPlayerDTO.setBoostPadAmount(amount);
    }
}
