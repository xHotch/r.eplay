package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.List;
import java.util.Objects;

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
        int count = 0;
        for (TeamDTO teamDTO: teams)
            if (count == 0) {
                ret += teamDTO.getName();
                count++;
            } else {
                ret += ", " + teamDTO.getName();
            }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerTeamsDTO that = (PlayerTeamsDTO) o;
        return Objects.equals(getPlayerDTO(), that.getPlayerDTO()) && Objects.equals(getTeams(), that.getTeams());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerDTO(), getTeams());
    }
}
