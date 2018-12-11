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
