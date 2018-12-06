package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import java.util.LinkedHashMap;

public class BallInformation {

    double frameTime;
    double frameDelta;

    LinkedHashMap<String,Object> position;

    public double getFrameTime() {
        return frameTime;
    }

    public void setFrameTime(double frameTime) {
        this.frameTime = frameTime;
    }

    public double getFrameDelta() {
        return frameDelta;
    }

    public void setFrameDelta(double frameDelta) {
        this.frameDelta = frameDelta;
    }

    public LinkedHashMap<String, Object> getPosition() {
        return position;
    }

    public void setPosition(LinkedHashMap<String, Object> position) {
        this.position = position;
    }
}
