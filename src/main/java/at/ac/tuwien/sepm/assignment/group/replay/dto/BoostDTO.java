package at.ac.tuwien.sepm.assignment.group.replay.dto;

public class BoostDTO {

    private double frameTime;
    private double frameDelta;
    private int frame;
    private int boost;

    private boolean gamePaused;

    public BoostDTO(double frameTime, double frameDelta, int frame, boolean gamePaused, int boost) {
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.frame = frame;
        this.gamePaused = gamePaused;
        this.boost = boost;
    }

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

    public boolean isGamePaused() {
        return gamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        this.gamePaused = gamePaused;
    }

    public int getBoostAmount() {
        return boost;
    }

    public void setBoostAmount(int boostAmount) {
        this.boost = boostAmount;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }
}
