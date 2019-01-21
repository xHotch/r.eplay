package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.List;

public class PlayerTeamsDTO {
    private PlayerDTO playerDTO;
    private List<TeamDTO> teams;

    public PlayerDTO getPlayerDTO() {
        return playerDTO;
    }

    public void setPlayerDTO(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    public List<TeamDTO> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamDTO> teams) {
        this.teams = teams;
    }

    public String getPlayerName() {
        return playerDTO.getName();
    }

    public String getTeamNames() {
        String ret = "";
        for (TeamDTO teamDTO: teams) {
            ret += teamDTO.getName() + "; ";
        }
        return ret;
    }
}
