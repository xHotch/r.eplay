package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.Objects;

/**
 * @author Gabriel Aichinger
 */
public class PlayerDTO {
    private long id;
    private String name;
    private long platformID;
    private boolean shown;

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

    public long getPlatformID() {
        return platformID;
    }

    public void setPlatformID(long platformID) {
        this.platformID = platformID;
    }

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    @Override
    public String toString() {
        return "PlayerDTO{" + "id=" + id + ", name='" + name + '\'' + ", platformID=" + platformID + ", shown=" + shown + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerDTO)) return false;
        PlayerDTO playerDTO = (PlayerDTO) o;
        return getId() == playerDTO.getId() && getPlatformID() == playerDTO.getPlatformID() && Objects.equals(getName(), playerDTO.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPlatformID());
    }
}
