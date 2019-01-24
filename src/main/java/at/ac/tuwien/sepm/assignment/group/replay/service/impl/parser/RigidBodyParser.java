package at.ac.tuwien.sepm.assignment.group.replay.service.impl.parser;

import at.ac.tuwien.sepm.assignment.group.replay.service.exception.FileServiceException;
import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import com.jayway.jsonpath.ReadContext;
import org.apache.commons.math3.complex.Quaternion;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RigidBodyParser {


    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ReadContext ctx;
    //Strings
    private String rigidBody = "['TAGame.RBActor_TA:ReplicatedRBState']";
    private String rigidBodyPosition = rigidBody + ".Position";
    private String rigidBodyRotation = rigidBody + ".Rotation";
    private String rigidBodyLinearVelocity = rigidBody + ".LinearVelocity";
    private String rigidBodyAngularVelocity = rigidBody + ".AngularVelocity";

    void setCtx(ReadContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Reads Information about RigidBodys from specific frame / actorUpdate
     *
     * @param frameId       ID of the frame to parse
     * @param actorUpdateId ID of the ActorUpdate to parse
     * @param frameTime     frameTime of the frame
     * @param gamePaused    boolean to indicate if the game is paused (at the Start, at a goal etc.) so we can calculate statistics properly from the returned values
     * @return RigidBodyInformation Containing rotation, velocity and position information, as well as frame and delta time values.
     * @throws FileServiceException if the file couldn't be parsed
     */
    RigidBodyInformation parseRigidBodyInformation(int frameId, int actorUpdateId, double frameTime, boolean gamePaused) throws FileServiceException {
        LOG.trace("Called - parseRigidBodyInformation");

        LinkedHashMap<String, Object> position = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyPosition);
        LinkedHashMap<String, Object> rotation = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyRotation);
        LinkedHashMap<String, Object> linearVelocity = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyLinearVelocity);
        LinkedHashMap<String, Object> angularVelocity = ctx.read("$.Frames[" + frameId + "].ActorUpdates[" + actorUpdateId + "]." + rigidBodyAngularVelocity);

        RigidBodyInformation rigidBodyInformation = new RigidBodyInformation();

        if (position != null) rigidBodyInformation.setPosition(getVectorFromMap(position));
        if (angularVelocity != null) rigidBodyInformation.setAngularVelocity(getVectorFromMap(angularVelocity));
        if (linearVelocity != null) rigidBodyInformation.setLinearVelocity(getVectorFromMap(linearVelocity));
        if (rotation != null) rigidBodyInformation.setRotation(getQuaternionFromMap(rotation));


        rigidBodyInformation.setGamePaused(gamePaused);
        rigidBodyInformation.setFrameTime(frameTime);


        return rigidBodyInformation;
    }


    /**
     * Generates a Vector3D from LinkedHashMap generated by JsonPath.
     * Vector3D are better for calculation, compared  to the map.
     * <p>
     * Velocities and Positions are stored as 3 dimensional Vectors
     *
     * @param map LinkedHashMap parsed by JsonPath, containing 3 dimensional Vector values
     * @return Vector3D containing the x,y,z values from the map
     */
    private Vector3D getVectorFromMap(LinkedHashMap<String, Object> map) {
        int i = 0;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                if (i == 0) {
                    x = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 1) {
                    y = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 2) {
                    z = ((Integer) entry.getValue()).doubleValue();
                }
            } else {
                if (i == 0) {
                    x = (double) entry.getValue();
                }
                if (i == 1) {
                    y = (double) entry.getValue();
                }
                if (i == 2) {
                    z = (double) entry.getValue();
                }
            }
            i++;
        }

        return new Vector3D(x, y, z);
    }

    /**
     * Generates a Quaternion from LinkedHashMap generated by JsonPath.
     * Quaternions are better for calculations, compared  to the map.
     * <p>
     * Rotations are stored as Quaternions
     *
     * @param map LinkedHashMap parsed by JsonPath, containing Quaternion values
     * @return Quaternion containing the x,x,y,z values from the map
     */
    private Quaternion getQuaternionFromMap(LinkedHashMap<String, Object> map) {
        int i = 0;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double w = 0.0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Integer) {
                if (i == 0) {
                    x = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 1) {
                    y = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 2) {
                    z = ((Integer) entry.getValue()).doubleValue();
                }
                if (i == 3) {
                    w = ((Integer) entry.getValue()).doubleValue();
                }
            } else {
                if (i == 0) {
                    x = (double) entry.getValue();
                }
                if (i == 1) {
                    y = (double) entry.getValue();
                }
                if (i == 2) {
                    z = (double) entry.getValue();
                }
                if (i == 3) {
                    w = (double) entry.getValue();
                }
            }
            i++;

        }

        //return new Quaternion(x,y,z,w);
        return new Quaternion(w, x, y, z);
    }
}
