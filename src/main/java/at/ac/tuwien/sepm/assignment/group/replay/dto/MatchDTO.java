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


    /**
     * Method that is used by FXML to gather Information for the {@link at.ac.tuwien.sepm.assignment.group.replay.ui.MainwindowController#tableColumnPlayersRed} recursively
     */
    public String getTeamRedPlayers(){
        return getTeamPlayers(0);
    }


    /**
     * Method that is used by FXML to gather Information for the {@link at.ac.tuwien.sepm.assignment.group.replay.ui.MainwindowController#tableColumnPlayersBlue} recursively
     */
    public String getTeamBluePlayers(){
        return getTeamPlayers(1);
    }



    /**
     * Helper method to get String containing the Player names from a team
     *
     * @param team the team id
     */
    private String getTeamPlayers(int team){

        StringBuilder players = new StringBuilder();

        for (MatchPlayerDTO player : playerData){
            if (player.getTeam() == team) {
                players.append(player.getName() + ", ");
            }
        }

        return players.toString();
    }

}
