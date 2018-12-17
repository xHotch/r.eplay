package at.ac.tuwien.sepm.assignment.group.replay.dto;

import org.jfree.data.xy.XYDataset;

import java.awt.image.BufferedImage;

/**
 * @author Daniel Klampfl
 */
public class HeatmapDTO {

    private XYDataset dataset;
    private double upperBound;
    private BufferedImage image;

    public XYDataset getDataset() {
        return dataset;
    }

    public void setDataset(XYDataset dataset) {
        this.dataset = dataset;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
