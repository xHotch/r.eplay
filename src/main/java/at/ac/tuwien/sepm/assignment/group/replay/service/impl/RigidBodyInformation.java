package at.ac.tuwien.sepm.assignment.group.replay.service.impl;

import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class RigidBodyInformation {

    private double frameTime;
    private double frameDelta;

    private boolean gamePaused;

    private Vector3D position;
    private Quaternion rotation;
    private Vector3D angularVelocity;
    private Vector3D linearVelocity;


    public boolean isGamePaused() {
        return gamePaused;
    }

    public void setGamePaused(boolean gamePaused) {
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

    public Vector3D getPosition() {
        return position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    public Vector3D getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(Vector3D angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public Vector3D getLinearVelocity() {
        return linearVelocity;
    }

    public void setLinearVelocity(Vector3D linearVelocity) {
        this.linearVelocity = linearVelocity;
    }


}
