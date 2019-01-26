package at.ac.tuwien.sepm.assignment.group.replay.dto;

/**
 * @author Bernhard Bayer
 */
public class GoalDTO {

    private double frameTime;
    private String playerName;
    private int teamSide;

    public double getFrameTime() {
        return frameTime;
    }

    public void setFrameTime(double frameTime) {
        this.frameTime = frameTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getTeamSide() {
        return teamSide;
    }

    public void setTeamSide(int teamSide) {
        this.teamSide = teamSide;
    }
}
