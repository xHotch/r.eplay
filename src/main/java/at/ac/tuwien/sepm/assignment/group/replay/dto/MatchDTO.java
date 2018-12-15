package at.ac.tuwien.sepm.assignment.group.replay.dto;

import at.ac.tuwien.sepm.assignment.group.replay.ui.MainWindowController;

import java.time.LocalDateTime;
import java.util.List;

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
     * Helper method to get String containing the Player names from a team
     *
     * @param team the team id
     */
    private String getTeamPlayers(TeamSide team){

        StringBuilder players = new StringBuilder();

        for (MatchPlayerDTO player : playerData){
            if (player.getTeam() == team) {
                players.append(player.getName() + ", ");
            }
        }

        return players.toString();
    }

}
