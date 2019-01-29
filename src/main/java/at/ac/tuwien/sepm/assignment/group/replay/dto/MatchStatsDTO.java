package at.ac.tuwien.sepm.assignment.group.replay.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MatchStatsDTO {

    private TeamSide team;
    private int score;
    private int goals;
    private int assists;
    private int shots;
    private int saves;
    private double averageSpeed;
    private double boostPerMinute;
    private int boostPadAmount;
    private LocalDateTime dateTime;
    private int matchId;
    private long teamId;

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

    public double getBoostPerMinute() {
        return boostPerMinute;
    }

    public void setBoostPerMinute(double boostPerMinute) {
        this.boostPerMinute = boostPerMinute;
    }

    public int getBoostPadAmount() {
        return boostPadAmount;
    }

    public void setBoostPadAmount(int boostPadAmount) {
        this.boostPadAmount = boostPadAmount;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getFormattedDateTime(){
        String res;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(("dd-MM-yyyy   HH:mm"));
        res = this.dateTime.format(formatter);
        return res;}

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
