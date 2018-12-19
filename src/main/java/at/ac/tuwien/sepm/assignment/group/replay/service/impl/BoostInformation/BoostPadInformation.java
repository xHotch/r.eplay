package at.ac.tuwien.sepm.assignment.group.replay.service.impl.BoostInformation;

// reference to https://github.com/RLBot/RLBot/wiki/Useful-Game-Values
public class BoostPadInformation {

    private double frameTime;
    private double frameDelta;
    private int frame;
    private int boostPadId;

    private boolean gamePaused;

    public BoostPadInformation(double frameTime, double frameDelta, int frame, boolean gamePaused, int boostPadId) {
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.frame = frame;
        this.gamePaused = gamePaused;
        this.boostPadId = boostPadId;

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

