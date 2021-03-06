package at.ac.tuwien.sepm.assignment.group.replay.dto;

import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;

import java.util.HashMap;
import java.util.Map;

public class FrameDTO {

  private double frameTime;

  public FrameDTO(double frameTime){
      this.frameTime = frameTime;
      this.carRigidBodyInformations = new HashMap<>();
  }

  //actorid to RBI
  private Map<Integer, RigidBodyInformation> carRigidBodyInformations;
  private RigidBodyInformation ballRigidBodyInformation;

    public double getFrameTime() {
        return frameTime;
    }

    public void setFrameTime(double frameTime) {
        this.frameTime = frameTime;
    }

    public Map<Integer, RigidBodyInformation> getCarRigidBodyInformations() {
        return carRigidBodyInformations;
    }

    public void setCarRigidBodyInformations(Map<Integer, RigidBodyInformation> carRigidBodyInformations) {
        this.carRigidBodyInformations = carRigidBodyInformations;
    }

    public RigidBodyInformation getBallRigidBodyInformation() {
        return ballRigidBodyInformation;
    }

    public void setBallRigidBodyInformation(RigidBodyInformation ballRigidBodyInformation) {
        this.ballRigidBodyInformation = ballRigidBodyInformation;
    }
}
