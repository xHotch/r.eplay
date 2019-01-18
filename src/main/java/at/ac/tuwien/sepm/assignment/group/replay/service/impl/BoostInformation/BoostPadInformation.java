package at.ac.tuwien.sepm.assignment.group.replay.service.impl.BoostInformation;

public class BoostPadInformation {

    private double frameTime;
    private double frameDelta;
    private int frame;
    private int boostPad;

    private boolean gamePaused;

    public BoostPadInformation(double frameTime, double frameDelta, int frame, boolean gamePaused, int boostPad) {
        this.frameTime = frameTime;
        this.frameDelta = frameDelta;
        this.frame = frame;
        this.gamePaused = gamePaused;
        this.boostPad = boostPad;
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

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public int getBoostPad() {
        return boostPad;
    }

    public void setBoostPad(int boostPad) {
        this.boostPad = boostPad;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
        this.gamePaused = gamePaused;
    }
}

