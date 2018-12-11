package at.ac.tuwien.sepm.assignment.group.replay.dto;

import java.util.Optional;

/**
 * @author Daniel Klampfl
 */
public enum TeamSide {
    RED(0),BLUE(1);

    private int id;

    TeamSide(int id) {
        this.id = id;
    }


    public static Optional<TeamSide> getById(int id) {
        for(TeamSide e : values()) {
            if(e.id == id) return Optional.of(e);
        }
        return Optional.empty();
    }

    public int getId() {
        return id;
    }
}
