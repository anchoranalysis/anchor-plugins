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

package ch.ethz.biol.cell.imageprocessing.stack.provider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.plot.PlotInstance;
import org.anchoranalysis.anchor.plot.io.GraphOutputter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.bufferedimage.CreateStackFromBufferedImage;

/** Displays a histogram */
public class StackProviderHistogram extends StackProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private HistogramProvider histogramProvider;

    /** Size of the image produced showing a plot of the histogram */
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(1024, 768);
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        try {
            List<HistogramItem> listHI = histogramList(histogramProvider.create());

            PlotInstance gi = HistogramPlot.create(listHI.iterator(), null, null);

            BufferedImage bi =
                    GraphOutputter.createBufferedImage(gi, size.getWidth(), size.getHeight());

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
}
