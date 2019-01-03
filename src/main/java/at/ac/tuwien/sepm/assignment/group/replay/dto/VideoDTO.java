package at.ac.tuwien.sepm.assignment.group.replay.dto;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoDTO {

    /**
     * @author Philipp Hochhauser
     */
    public VideoDTO(){
        this.frames=new ArrayList<>();
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
