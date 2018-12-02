package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 */
public class MatchDTO {
    
    private int id;
    private LocalDateTime dateTime;
    private int teamSize;
    private List<MatchPlayerDTO> playerData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public List<MatchPlayerDTO> getPlayerData() {
        return playerData;
    }

    public void setPlayerData(List<MatchPlayerDTO> playerData) {
        this.playerData = playerData;
    }
}
