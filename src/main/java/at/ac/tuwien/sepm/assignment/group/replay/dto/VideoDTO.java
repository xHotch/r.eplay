package at.ac.tuwien.sepm.assignment.group.replay.dto;


import org.apache.commons.math3.util.Pair;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VideoDTO {

    private Map<Long, Integer> actorIds;

    private MultiValueMap<Integer, Pair<Integer, Double>> playerToCarAndTimeMap;

    private Map<Integer, Integer> carActorIds;

    private List<FrameDTO> frames;

    private List<GoalDTO> goals;


    public VideoDTO(){
        this.frames=new ArrayList<>();
    }

    public MultiValueMap<Integer, Pair<Integer, Double>> getPlayerToCarAndTimeMap() {
        return playerToCarAndTimeMap;
    }

    public void setPlayerToCarAndTimeMap(MultiValueMap<Integer, Pair<Integer, Double>> playerToCarAndTimeMap) {
        this.playerToCarAndTimeMap = playerToCarAndTimeMap;
    }

    public Map<Integer, Integer> getCarActorIds() {
        return carActorIds;
    }

    public void setCarActorIds(Map<Integer, Integer> carActorIds) {
        this.carActorIds = carActorIds;
    }


    public Map<Long, Integer> getActorIds() {
        return actorIds;
    }

    public void setActorIds(Map<Long, Integer> actorIds) {
        this.actorIds = actorIds;
    }


    public List<FrameDTO> getFrames() {
        return frames;
    }

    public void setFrames(List<FrameDTO> frames) {
        this.frames = frames;
    }

    public List<GoalDTO> getGoals() {
        return goals;
    }

    public void setGoals(List<GoalDTO> goals) {
        this.goals = goals;
    }

}
