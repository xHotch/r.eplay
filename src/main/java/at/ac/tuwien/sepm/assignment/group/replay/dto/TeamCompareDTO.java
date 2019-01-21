package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.List;
import java.util.Map;

/**
 * @author Daniel Klampfl
 */
public class TeamCompareDTO {
    /**
     * key: matchid value liste with 2 MatchStatDTOs for each teamSide
     */
    private Map<Integer,List<MatchStatsDTO>> matchStatsDTOList;
    private List<MatchDTO> matchDTOList;

    public Map<Integer, List<MatchStatsDTO>> getMatchStatsDTOList() {
        return matchStatsDTOList;
    }

    public void setMatchStatsDTOList(Map<Integer, List<MatchStatsDTO>> matchStatsDTOList) {
        this.matchStatsDTOList = matchStatsDTOList;
    }

    public List<MatchDTO> getMatchDTOList() {
        return matchDTOList;
    }

    public void setMatchDTOList(List<MatchDTO> matchDTOList) {
        this.matchDTOList = matchDTOList;
    }
}
