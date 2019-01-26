package at.ac.tuwien.sepm.assignment.group.replay.service.impl.statistic;

import at.ac.tuwien.sepm.assignment.group.replay.service.impl.RigidBodyInformation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tc33.jheatchart.HeatChart;

import java.awt.*;
import java.awt.image.*;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * @author Daniel Klampfl
 */
@Service
public class RigidBodyStatistic {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final double AIR_THRESHOLD = 30.0;

    private double averageSpeed;
    private double positiveSideTime = 0;
    private double negativeSideTime = 0;
    private double airTime;
    private double groundTime;

    double averageDistanceTo(List<RigidBodyInformation> rigidBodyInformation1, List<RigidBodyInformation> rigidBodyInformation2) {
        LOG.trace("Called - averageDistanceTo");
        RigidBodyInformation[] rigidBodyList1 = rigidBodyInformation1.toArray(new RigidBodyInformation[0]);
        RigidBodyInformation[] rigidBodyList2 = rigidBodyInformation2.toArray(new RigidBodyInformation[0]);
        Vector3D positionBody1 = rigidBodyList1[0].getPosition();
        Vector3D positionBody2 = rigidBodyList2[0].getPosition();
        double distance = 0;
        int count = 0;
        int i = 0;
        int j = 0;
        while (i < rigidBodyList1.length && j < rigidBodyList2.length) {
            if (rigidBodyList1[i].getFrameTime() == rigidBodyList2[j].getFrameTime()) {
                positionBody1 = rigidBodyList1[i].getPosition();
                positionBody2 = rigidBodyList2[j].getPosition();
                i++;
                j++;
            } else if (rigidBodyList1[i].getFrameTime() < rigidBodyList2[j].getFrameTime()) {
                positionBody1 = rigidBodyList1[i].getPosition();
                i++;
            } else {
                positionBody2 = rigidBodyList2[j].getPosition();
                j++;
            }
            distance += positionBody1.distance(positionBody2);
            count++;
        }
        return count != 0 ? distance / count : 0;
    }

    public void calculate(List<RigidBodyInformation> rigidBodyInformations) {
        LOG.trace("Called calculate");
        RigidBodyInformation[] rigidBodyList = rigidBodyInformations.toArray(new RigidBodyInformation[0]);
        averageSpeed = 0;
        positiveSideTime = 0;
        negativeSideTime = 0;
        airTime = 0;
        groundTime = 0;
        double deltaTime;
        double distance;
        double frameSpeed;
        double speed = 0;
        int count = 0;
        int countFrame = 0;
        for (int i = 1; i < rigidBodyList.length - 1; i++) {
            RigidBodyInformation rigidBody1 = rigidBodyList[i];
            RigidBodyInformation rigidBody2 = rigidBodyList[i + 1];
            deltaTime = rigidBody2.getFrameTime() - rigidBody1.getFrameTime();
            distance = rigidBody1.getPosition().distance(rigidBody2.getPosition());
            if (!rigidBody1.isGamePaused() && !rigidBody2.isGamePaused()) {
                //side Time
                if (rigidBody1.getPosition().getY() < 0) negativeSideTime += deltaTime;
                else positiveSideTime += deltaTime;
                //ground / air time
                if (rigidBody1.getPosition().getZ() < AIR_THRESHOLD) groundTime += deltaTime;
                else airTime += deltaTime;
                //average Speed
                frameSpeed = distance / deltaTime;
                speed += frameSpeed;
                countFrame++;
            } else count++;
        }
        if (countFrame > 0) averageSpeed = speed / countFrame;
        else averageSpeed = 0;
        LOG.debug("Speed {} Count {} CountFrame {} negativeSideTime {} positiveSideTime {} groundTime {} airTime {}", averageSpeed, count, countFrame, negativeSideTime, positiveSideTime, groundTime, airTime);
    }

    BufferedImage calculateHeatmap(List<RigidBodyInformation> rigidBodyList)
    {
        int fieldWidth = 12240;
        int fieldHeight = 8192;
        int divideFactor = 100;
        int width = fieldWidth / divideFactor + 1;
        int height = fieldHeight / divideFactor + 1;
        double[][] heatmapData = new double[width][height];
        for (RigidBodyInformation rigidBody : rigidBodyList) {
            int x = ((int) Math.round(rigidBody.getPosition().getX())) + 4096;
            int y = ((int) Math.round(rigidBody.getPosition().getY())) + 6120;
            if(y >= 0 && x >= 0 && y <= fieldWidth && x <= fieldHeight)
            {
                int iy = y / divideFactor;
                int ix = x / divideFactor;
                if(iy < width && ix < height) heatmapData[iy][ix] += 1;
                else LOG.debug("Coordinate not in array x: {} y: {}",ix,iy);
            }
            else LOG.debug("Coordinate not in dimensions x: {} y: {}",x,y);
        }

        HeatChart heatChart = new HeatChart(heatmapData);
        heatChart.setColourScale(0.5);
        heatChart.setLowValueColour(Color.WHITE);
        heatChart.setHighValueColour(Color.RED);

        BufferedImage heatmap = ((BufferedImage) heatChart.getChartImage());
        BufferedImage cropped = ((BufferedImage) heatChart.getChartImage()).getSubimage(50, 40 , heatmap.getWidth() -60 ,heatmap.getHeight()-140);
        Image transpImg = transformWhiteToTransparency(cropped);
        return imageToBufferedImage(transpImg, heatmap.getWidth()-60, heatmap.getHeight()-180);
    }


    private Image transformWhiteToTransparency(BufferedImage image)
    {
        // Primitive test, just an example
        final int r1 = 255;
        final int g1 = 255;
        final int b1 = 255;
        ImageFilter filter = new RGBImageFilter()
        {
            public final int filterRGB(int x, int y, int rgb)
            {
                int r = (rgb & 0xFF0000) >> 16;
                int g = (rgb & 0xFF00) >> 8;
                int b = rgb & 0xFF;
                if (r >= r1 &&
                    g >= g1 &&
                    b >= b1)
                {
                    // Set fully transparent but keep color
                    return rgb & 0xFFFFFF;
                }
                return rgb;
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    private BufferedImage imageToBufferedImage(Image image, int width, int height)
    {
        BufferedImage dest = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return dest;
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
