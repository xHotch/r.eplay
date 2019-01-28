package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private int boostPadAmount;
    private double boostPerMinute;
    private double timeFullBoost;
    private double timeLowBoost;


    private BufferedImage heatmapImage;
    private String heatmapFilename;
    private Map<Integer, List<BoostPadDTO>> boostPadMap;
    private Map<Integer, List<Integer>> dbBoostPadMap;

    private int actorId;

    private int teamActorId;

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

    public int getAverageSpeedAsInt() {
        return (int) averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getAverageDistanceToBall() {
        return averageDistanceToBall;
    }

    public void setAverageDistanceToBall(double averageDistanceToBall) {
        this.averageDistanceToBall = averageDistanceToBall;
    }

    public double getTimeFullBoost() {
        return timeFullBoost;
    }

    public void setTimeFullBoost(double timeFullBoost) {
        this.timeFullBoost = timeFullBoost;
    }

    public double getTimeLowBoost() {
        return timeLowBoost;
    }

    public void setTimeLowBoost(double timeLowBoost) {
        this.timeLowBoost = timeLowBoost;
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

    public String getHeatmapFilename() {
        return heatmapFilename;
    }

    public void setHeatmapFilename(String heatmapFilename) {
        this.heatmapFilename = heatmapFilename;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public Map<Integer, List<BoostPadDTO>> getBoostPadMap() {
        return boostPadMap;
    }

    public void setBoostPadMap(Map<Integer, List<BoostPadDTO>> boostPadMap) {
        this.boostPadMap = boostPadMap;
    }

    public Map<Integer, List<Integer>> getDBBoostPadMap() {
        return dbBoostPadMap;
    }

    public void setDBBoostPadMap(Map<Integer, List<Integer>> boostPadMap) {
        this.dbBoostPadMap = boostPadMap;
    }

    public int getTeamActorId() {
        return teamActorId;
    }

    public void setTeamActorId(int teamActorId) {
        this.teamActorId = teamActorId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchPlayerDTO that = (MatchPlayerDTO) o;
        return getScore() == that.getScore() && getGoals() == that.getGoals() && getAssists() == that.getAssists() && getShots() == that.getShots() && getSaves() == that.getSaves() && Double.compare(that.getHomeSideTime(), getHomeSideTime()) == 0 && Double.compare(that.getEnemySideTime(), getEnemySideTime()) == 0 && Double.compare(that.getAverageSpeed(), getAverageSpeed()) == 0 && Double.compare(that.getAverageDistanceToBall(), getAverageDistanceToBall()) == 0 && Double.compare(that.getAirTime(), getAirTime()) == 0 && Double.compare(that.getGroundTime(), getGroundTime()) == 0 && getActorId() == that.getActorId() && getTeamActorId() == that.getTeamActorId() && Objects.equals(getPlayerDTO(), that.getPlayerDTO()) && getTeam() == that.getTeam() && Objects.equals(getHeatmapImage(), that.getHeatmapImage()) && Objects.equals(getHeatmapFilename(), that.getHeatmapFilename()) && Objects.equals(getBoostPadMap(), that.getBoostPadMap()) && Objects.equals(dbBoostPadMap, that.dbBoostPadMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerDTO(), getTeam(), getScore(), getGoals(), getAssists(), getShots(), getSaves(), getHomeSideTime(), getEnemySideTime(), getAverageSpeed(), getAverageDistanceToBall(), getAirTime(), getGroundTime(), getHeatmapImage(), getHeatmapFilename(), getBoostPadMap(), dbBoostPadMap, getActorId(), getTeamActorId());
    }
}
