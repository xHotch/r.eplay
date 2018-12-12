package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * @author Daniel Klampfl
 */
class RigidBodyStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    ArrayList<RigidBodyInformation> rigidBodyInformations = new ArrayList<>();

    private float averageSpeed;
    private float homeSideTime;
    private float enemySideTime;
    private float airTime;
    private float groundTime;

    public RigidBodyStatistic() {
    }

    float averagerDistanceTo(ArrayList<RigidBodyInformation> list) {
        return 0;
    }

    void calculate()
    {
        ListIterator list = rigidBodyInformations.listIterator();
        for (RigidBodyInformation rigidBody : rigidBodyInformations) {

        }
    }

    public void setRigidBodyInformations(ArrayList<RigidBodyInformation> rigidBodyInformations) {
        this.rigidBodyInformations = rigidBodyInformations;
    }

    float getAverageSpeed() {
        return averageSpeed;
    }

    float getHomeSideTime() {
        return homeSideTime;
    }

    float getEnemySideTime() {
        return enemySideTime;
    }

    float getAirTime() {
        return airTime;
    }

    float getGroundTime() {
        return groundTime;
    }
}
