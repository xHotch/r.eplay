package at.ac.tuwien.sepm.assignment.group.replay.dto;

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

}
