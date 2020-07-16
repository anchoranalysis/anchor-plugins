/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.anchor.plot.GraphInstance;
import org.anchoranalysis.anchor.plot.io.GraphOutputter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.bufferedimage.CreateStackFromBufferedImage;

/** Displays a histogram */
public class StackProviderHistogram extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField private HistogramProvider histogramProvider;

    @BeanField @Positive private int width;

    @BeanField @Positive private int height;
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        try {
            List<HistogramItem> listHI = histogramList(histogramProvider.create());

            GraphInstance gi = HistogramPlot.create(listHI.iterator(), null, null);

            BufferedImage bi = GraphOutputter.createBufferedImage(gi, width, height);

            return CreateStackFromBufferedImage.create(bi);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static List<HistogramItem> histogramList(Histogram histogram) {
        ArrayList<HistogramItem> listHI = new ArrayList<>();
        for (int i = 1; i < histogram.size(); i++) {
            HistogramItem hi = new HistogramItem(i, histogram.getCount(i));
            listHI.add(hi);
        }
        return listHI;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public HistogramProvider getHistogramProvider() {
        return histogramProvider;
    }

    public void setHistogramProvider(HistogramProvider histogramProvider) {
        this.histogramProvider = histogramProvider;
    }
}
