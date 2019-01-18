package at.ac.tuwien.sepm.assignment.group.replay.dto;

// reference to https://github.com/RLBot/RLBot/wiki/Useful-Game-Values
public class BoostPadDTO {

    private double frameTime;
    private double frameDelta;
    private int frame;
    private int boostPadId;

    private boolean gamePaused;

    public BoostPadDTO(double frameTime, double frameDelta, int frame, boolean gamePaused) {
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.frame = frame;
        this.gamePaused = gamePaused;
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

    public int getBoostPadId() {
        return boostPadId;
    }

    public void setBoostPadId(int boostPad) {
        this.boostPadId = boostPadId;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }
}

