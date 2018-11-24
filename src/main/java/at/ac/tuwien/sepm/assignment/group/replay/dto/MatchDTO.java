package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.time.LocalDateTime;

/**
 *
 */
public class MatchDTO {
    private LocalDateTime dateTime;

    private int teamBlueGoals;
    private int teamRedGoals;
    private int teamSize;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public int getTeamBlueGoals() {
        return teamBlueGoals;
    }

    public void setTeamBlueGoals(int teamBlueGoals) {
        this.teamBlueGoals = teamBlueGoals;
    }

    public int getTeamRedGoals() {
        return teamRedGoals;
    }

    public void setTeamRedGoals(int teamRedGoals) {
        this.teamRedGoals = teamRedGoals;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }
}
