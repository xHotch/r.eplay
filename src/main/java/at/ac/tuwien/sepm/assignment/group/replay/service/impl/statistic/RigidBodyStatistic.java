package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * @author Daniel Klampfl
 */
@Service
public class RigidBodyStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    RigidBodyInformation[] rigidBodyList;

    private double averageSpeed;
    private double positiveSideTime = 0;
    private double negativeSideTime = 0;
    private double airTime;
    private double groundTime;

    public RigidBodyStatistic() {
    }

    float averagerDistanceTo(List<RigidBodyInformation> list) {
        return 0;
    }

    public void calculate()
    {
        double deltaTime;
        double distance;
        double frameSpeed;
        double speed = 0;
        int count = 0;
        int countFrame = 0;
        for (int i = 1; i < rigidBodyList.length-1; i++) {
            RigidBodyInformation rigidBody1 = rigidBodyList[i];
            RigidBodyInformation rigidBody2 = rigidBodyList[i+1];
            deltaTime = rigidBody2.getFrameTime() - rigidBody1.getFrameTime();
            distance = rigidBody1.getPosition().distance(rigidBody2.getPosition());
            if(!rigidBody1.isGamePaused() && !rigidBody2.isGamePaused()) {
                if(rigidBody1.getPosition().getX() < 0) negativeSideTime += deltaTime;
                else positiveSideTime += deltaTime;
                if(rigidBody1.getPosition().getZ() < 18) groundTime += deltaTime;
                else airTime += deltaTime;
                frameSpeed = distance / deltaTime;
                speed += frameSpeed;
                countFrame++;
            } else count++;
        }
        if (countFrame > 0) averageSpeed = speed/countFrame;
        else averageSpeed = 0;
        LOG.debug("Speed {} Count {} CountFrame {} negativeSideTime {} positiveSideTime {} groundTime {} airTime {}",averageSpeed,count,countFrame,negativeSideTime,positiveSideTime,groundTime,airTime);
    }

    public void setRigidBodyInformations(List<RigidBodyInformation> rigidBodyInformations) {
        this.rigidBodyList = rigidBodyInformations.toArray(new RigidBodyInformation[rigidBodyInformations.size()]);
    }

    double getAverageSpeed() {
        return averageSpeed;
    }

    double getPositiveSideTime() {
        return positiveSideTime;
    }

    double getNegativeSideTime() {
        return negativeSideTime;
    }

    double getAirTime() {
        return airTime;
    }

    double getGroundTime() {
        return groundTime;
    }
}
