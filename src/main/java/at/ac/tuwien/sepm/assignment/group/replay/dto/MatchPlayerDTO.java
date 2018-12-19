package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.awt.image.BufferedImage;

/**
 * @author Daniel Klampfl
 */
public class MatchPlayerDTO {


    private MatchDTO matchDTO;
    private PlayerDTO playerDTO;

    private TeamSide team; //is 0 for team RED and 1 for team BLUE
    private int score;
    private int goals;
    private int assists;
    private int shots;
    private int saves;
    private double homeSideTime;
    private double enemySideTime;
    private double averageSpeed;
    private double averageDistanceToBall;
    private double airTime;
    private double groundTime;
    private BufferedImage heatmapImage;

    private int actorId;

    public MatchDTO getMatchDTO() {
        return matchDTO;
    }

    public void setMatchDTO(MatchDTO matchDTO) {
        this.matchDTO = matchDTO;
    }

    public PlayerDTO getPlayerDTO() {
        return playerDTO;
    }

    public void setPlayerDTO(PlayerDTO playerDTO) {
        this.playerDTO = playerDTO;
    }

    public int getMatchId() {
        return matchDTO.getId();
    }

    public long getPlayerId() {
        return playerDTO.getId();
    }

    public String getName() {
        return playerDTO.getName();
    }

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

    public double getHomeSideTime() {
        return homeSideTime;
    }

    public void setHomeSideTime(double homeSideTime) {
        this.homeSideTime = homeSideTime;
    }

    public double getEnemySideTime() {
        return enemySideTime;
    }

    public void setEnemySideTime(double enemySideTime) {
        this.enemySideTime = enemySideTime;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public int getAverageSpeedAsInt() {return (int) averageSpeed;}

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getAverageDistanceToBall() {
        return averageDistanceToBall;
    }

    public void setAverageDistanceToBall(double averageDistanceToBall) {
        this.averageDistanceToBall = averageDistanceToBall;
    }

    public double getAirTime() {
        return airTime;
    }

    public void setAirTime(double airTime) {
        this.airTime = airTime;
    }

    public double getGroundTime() {
        return groundTime;
    }

    public void setGroundTime(double groundTime) {
        this.groundTime = groundTime;
    }

    public BufferedImage getHeatmapImage() {
        return heatmapImage;
    }

    public void setHeatmapImage(BufferedImage heatmapImage) {
        this.heatmapImage = heatmapImage;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

}
