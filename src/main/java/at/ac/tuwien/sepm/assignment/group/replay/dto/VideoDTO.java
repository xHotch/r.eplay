package at.ac.tuwien.sepm.assignment.group.replay.dto;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoDTO {

    /**
     * @author Philipp Hochhauser
     */
    public VideoDTO(){
        this.frames=new ArrayList<>();
    }

    private MatchDTO matchDTO;

    private Map<Long, Integer> actorIds;

    public Map<Long, Integer> getActorIds() {
        return actorIds;
    }

    public void setActorIds(Map<Long, Integer> actorIds) {
        this.actorIds = actorIds;
    }

    //frames
    private List<FrameDTO> frames;

    public List<FrameDTO> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameDTO> frames) {
        this.frames = frames;
    }



}
