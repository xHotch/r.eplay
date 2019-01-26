package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.List;
import java.util.Objects;

public class TeamDTO {
    private long id;
    private String name;
    private int teamSize;
    private List<PlayerDTO> players;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    @Override
    public String toString() {
        return "TeamDTO{" + "id=" + id + ", name='" + name + '\'' + ", teamSize=" + teamSize + ", players=" + players + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamDTO teamDTO = (TeamDTO) o;
        return getId() == teamDTO.getId() && getTeamSize() == teamDTO.getTeamSize() && Objects.equals(getName(), teamDTO.getName()) && Objects.equals(getPlayers(), teamDTO.getPlayers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getTeamSize(), getPlayers());
    }
}
