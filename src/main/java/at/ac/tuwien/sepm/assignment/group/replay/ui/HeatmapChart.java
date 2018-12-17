package at.ac.tuwien.sepm.assignment.group.replay.ui;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.invoke.MethodHandles;

/**
 * @author Daniel Klampfl
 */
public class HeatmapChart {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int N = 1000;
    /**
     * Heatmap init with JFreeChart
     * @param dataset
     * @return
     */
    public static JFreeChart createChart(XYDataset dataset, double upperbound)
    {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setVisible(false);
        yAxis.setVisible(false);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        XYBlockRenderer r = new XYBlockRenderer();
        SpectrumPaintScale ps = new SpectrumPaintScale(0,upperbound);
        r.setPaintScale(ps);
        r.setBlockHeight(1.0f);
        r.setBlockWidth(1.0f);
        plot.setRenderer(r);
        LOG.debug("Zoomable Domain: {} Range: {}",plot.isDomainZoomable(),plot.isRangeZoomable());
        JFreeChart chart = new JFreeChart(null,null, plot,false);
        chart.setBackgroundPaint(Color.gray);
        return chart;
    }

    /**
     * Example Data for heatmap
     * @return
     */
    public static XYDataset createDataset() {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        for (int i = 0; i < N; i = i + 10) {
            double[][] data = new double[3][N];
            for (int j = 0; j < N; j = j + 10) {
                data[0][j] = i;
                data[1][j] = j;
                data[2][j] = i * j;
            }
            dataset.addSeries("Series" + i, data);
        }
        return dataset;
    }

    /**
     * class for the colorspectrum of the heatmap
     */
    public static class SpectrumPaintScale implements PaintScale {

        private static final float H1 = 0.0f;
        private static final float H2 = 0.7f;
        private double lowerBound;
        private double upperBound;

        public SpectrumPaintScale(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public double getLowerBound() {
            return lowerBound;
        }

        @Override
        public double getUpperBound() {
            return upperBound;
        }

        @Override
        public Paint getPaint(double value) {

            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
            float scaledH = H1 + scaledValue * (H2 - H1);
            return Color.getHSBColor(scaledH, 1f, 1f);
        }
    }
}
