package at.ac.tuwien.sepm.assignment.group.replay.dto;

public class AvgStatsDTO {
    private double goals;
    private double assists;
    private double shots;
    private double saves;
    private double score;
    private double boostpads;
    private double boost;
    private double speed;
    private int wins;
    private int losses;

    public double getGoals() {
        return goals;
    }

    public void setGoals(double goals) {
        this.goals = goals;
    }

    public double getAssists() {
        return assists;
    }

    public void setAssists(double assists) {
        this.assists = assists;
    }

    public double getShots() {
        return shots;
    }

    public void setShots(double shots) {
        this.shots = shots;
    }

    public double getSaves() {
        return saves;
    }

    public void setSaves(double saves) {
        this.saves = saves;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getBoostpads() {
        return boostpads;
    }

    public void setBoostpads(double boostpads) {
        this.boostpads = boostpads;
    }

    public double getBoost() {
        return boost;
    }

    public void setBoost(double boost) {
        this.boost = boost;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }
}
