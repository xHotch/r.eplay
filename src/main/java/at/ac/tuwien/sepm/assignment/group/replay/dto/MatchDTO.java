package at.ac.tuwien.sepm.assignment.group.replay.dto;

import at.ac.tuwien.sepm.assignment.group.replay.ui.MainWindowController;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class MatchDTO {
    
    private int id;
    private LocalDateTime dateTime;
    private int teamSize;
    private String readId; //match id from json file
    private List<MatchPlayerDTO> playerData;
    private double timeBallInBlueSide;
    private double timeBallInRedSide;
    private int possessionBlue;
    private int possessionRed;
    private String replayFilename;
    private BufferedImage ballHeatmapImage;
    private String ballHeatmapFilename;
    private double matchTime;

    public String getReplayFilename() {
        return replayFilename;
    }

    public void setReplayFilename(String replayFilename) {
        this.replayFilename = replayFilename;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public String getReadId() {
        return readId;
    }

    public void setReadId(String readId) {
        this.readId = readId;
    }

    public List<MatchPlayerDTO> getPlayerData() {
        return playerData;
    }

    public void setPlayerData(List<MatchPlayerDTO> playerData) {
        this.playerData = playerData;
    }

    public double getTimeBallInBlueSide() {
        return timeBallInBlueSide;
    }

    public void setTimeBallInBlueSide(double timeBallInBlueSide) {
        this.timeBallInBlueSide = timeBallInBlueSide;
    }

    public double getTimeBallInRedSide() {
        return timeBallInRedSide;
    }

    public void setTimeBallInRedSide(double timeBallInRedSide) {
        this.timeBallInRedSide = timeBallInRedSide;
    }

    public int getPossessionBlue() {
        return possessionBlue;
    }

    public void setPossessionBlue(int possessionBlue) {
        this.possessionBlue = possessionBlue;
    }

    public int getPossessionRed() {
        return possessionRed;
    }

    public void setPossessionRed(int possessionRed) {
        this.possessionRed = possessionRed;
    }

    public BufferedImage getBallHeatmapImage() {
        return ballHeatmapImage;
    }

    public void setBallHeatmapImage(BufferedImage ballHeatmapImage) {
        this.ballHeatmapImage = ballHeatmapImage;
    }

    public String getBallHeatmapFilename() {
        return ballHeatmapFilename;
    }

    public void setBallHeatmapFilename(String ballHeatmapFilename) {
        this.ballHeatmapFilename = ballHeatmapFilename;
    }

    public double getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(double matchTime) {
        this.matchTime = matchTime;
    }

    /**
     * Method that is used by FXML to gather Information for the {@link MainWindowController#tableColumnPlayersRed} recursively
     */
    public String getTeamRedPlayers(){
        return getTeamPlayers(TeamSide.RED);
    }


    /**
     * Method that is used by FXML to gather Information for the {@link MainWindowController#tableColumnPlayersBlue} recursively
     */
    public String getTeamBluePlayers(){
        return getTeamPlayers(TeamSide.BLUE);
    }

    /**
     * Method to get String containing the result of the match. E.g. "4 : 5", where the team of the blue side
     *  scored 4 and the team of the red side scored 5 goals
     */
    public String getResult(){
        int redGoals = 0;
        int blueGoals = 0;
        for (MatchPlayerDTO player : playerData) {
            // team blue
            if (player.getTeam() == TeamSide.BLUE) {
                blueGoals += player.getGoals();
            }
            if (player.getTeam() == TeamSide.RED) {
                redGoals += player.getGoals();
            }
        }
        return "" + blueGoals + " : " + redGoals;
    }

    /**
     * Helper method to get String containing the Player names from a team
     *
     * @param team the team id
     */
    private String getTeamPlayers(TeamSide team){

        StringBuilder players = new StringBuilder();

        boolean setComma = false;
        for (MatchPlayerDTO player : playerData){
            if (player.getTeam() == team) {
                if (setComma){
                players.append(", " + player.getName());
                }
                else {players.append(player.getName());
                setComma = true;}
            }
        }

        return players.toString();
    }

    public String getMatchType(){
        return "" + teamSize + " vs " + teamSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchDTO matchDTO = (MatchDTO) o;
        return getTeamSize() == matchDTO.getTeamSize() && Double.compare(matchDTO.getTimeBallInBlueSide(), getTimeBallInBlueSide()) == 0 && Double.compare(matchDTO.getTimeBallInRedSide(), getTimeBallInRedSide()) == 0 && getPossessionBlue() == matchDTO.getPossessionBlue() && getPossessionRed() == matchDTO.getPossessionRed() && Double.compare(matchDTO.getMatchTime(), getMatchTime()) == 0 && Objects.equals(getDateTime(), matchDTO.getDateTime()) && Objects.equals(getReadId(), matchDTO.getReadId()) && Objects.equals(getPlayerData(), matchDTO.getPlayerData()) && Objects.equals(getBallHeatmapImage(), matchDTO.getBallHeatmapImage()) && Objects.equals(getBallHeatmapFilename(), matchDTO.getBallHeatmapFilename());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDateTime(), getTeamSize(), getReadId(), getPlayerData(), getTimeBallInBlueSide(), getTimeBallInRedSide(), getPossessionBlue(), getPossessionRed(), getBallHeatmapImage(), getBallHeatmapFilename(), getMatchTime());
    }
}
