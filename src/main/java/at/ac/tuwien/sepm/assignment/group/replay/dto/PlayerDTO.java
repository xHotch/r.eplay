package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.Objects;

/**
 * @author Gabriel Aichinger
 */
public class PlayerDTO {
    private int id;
    private String name;
    private long plattformid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPlattformid() {
        return plattformid;
    }

    public void setPlattformid(long plattformid) {
        this.plattformid = plattformid;
    }

    @Override
    public String toString() {
        return "PlayerDTO{" + "id=" + id + ", name='" + name + '\'' + ", plattformid=" + plattformid + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerDTO)) return false;
        PlayerDTO playerDTO = (PlayerDTO) o;
        return getId() == playerDTO.getId() && getPlattformid() == playerDTO.getPlattformid() && Objects.equals(getName(), playerDTO.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPlattformid());
    }
}
