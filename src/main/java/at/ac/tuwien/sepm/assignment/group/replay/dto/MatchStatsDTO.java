package at.ac.tuwien.sepm.assignment.group.replay.dto;


public class MatchStatsDTO {

    private TeamSide team;
    private int score;
    private int goals;
    private int assists;
    private int shots;
    private int saves;
    private double averageSpeed;
    private boolean teamDTO;

    public TeamSide getTeam() {
        return team;
    }

    public void setTeam(TeamSide team) {
        this.team = team;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGoals() {
        return goals;
    }

    public void setGoals(int goals) {
        this.goals = goals;
    }

    public int getAssists() {
        return assists;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public int getShots() {
        return shots;
    }

    public void setShots(int shots) {
        this.shots = shots;
    }

    public int getSaves() {
        return saves;
    }

    public void setSaves(int saves) {
        this.saves = saves;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public boolean isTeamDTO() {
        return teamDTO;
    }

    public void setTeamDTO(boolean teamDTO) {
        this.teamDTO = teamDTO;
    }
}
