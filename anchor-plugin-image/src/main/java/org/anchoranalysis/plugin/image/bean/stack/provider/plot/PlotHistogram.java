/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.stack.provider.plot;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.bufferedimage.CreateStackFromBufferedImage;
import org.anchoranalysis.plot.AxisLimits;
import org.anchoranalysis.plot.PlotInstance;
import org.anchoranalysis.plot.bean.colorscheme.PlotColorScheme;
import org.anchoranalysis.plot.index.LinePlot;

/** 
 * Plots a histogram onto a stack.
 *
 * @author Owen Feehan
 */
public class PlotHistogram extends StackProvider {

    // START BEAN PROPERTIES
    /** The histogram to plot. */
    @BeanField @Getter @Setter private HistogramProvider histogram;

    /** Size of the image produced showing a plot of the histogram */
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(1024, 768);
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        try {
            List<HistogramBin> histogramItems = binsFromHistogram(histogram.create());

            PlotInstance plot =
                    createPlot(histogramItems.iterator(), Optional.empty(), Optional.empty());

            BufferedImage image =
                    plot.createBufferedImage(size.getWidth(), size.getHeight());

            return CreateStackFromBufferedImage.create(image);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static PlotInstance createPlot(
            Iterator<HistogramBin> itr,
            Optional<AxisLimits> domainLimits,
            Optional<AxisLimits> rangeLimits)
            throws CreateException {

        PlotColorScheme graphColorScheme = new PlotColorScheme();

        LinePlot<HistogramBin> plot =
                new LinePlot<>(
                        "Histogram",
                        new String[] {"Histogram"},
                        (HistogramBin item, int yIndex) -> (double) item.getCount());
        plot.getLabels().setX("Intensity");
        plot.getLabels().setY("Voxel Count");
        plot.setGraphColorScheme(graphColorScheme);

        return plot.create(itr, domainLimits, rangeLimits);
    }

    private static List<HistogramBin> binsFromHistogram(Histogram histogram) {
        return FunctionalList.mapRangeToList(1, histogram.size(), index -> new HistogramBin(index, histogram.getCount(index)) );
    }
}
