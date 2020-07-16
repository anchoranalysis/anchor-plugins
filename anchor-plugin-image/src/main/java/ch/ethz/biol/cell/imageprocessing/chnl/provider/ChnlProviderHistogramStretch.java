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
/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public class ChnlProviderHistogramStretch extends ChnlProviderOne {

    // START BEAN PROPERTIES
    @BeanField private double quantile = 1.0;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChnl(Channel chnl) throws CreateException {
        try {
            histogramStretch(chnl, quantile);
            return chnl;
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static void histogramStretch(Channel chnl, double quantile)
            throws OperationFailedException {

        VoxelBoxWrapper vb = chnl.getVoxelBox();

        Histogram hist = HistogramFactory.create(vb);

        double rangeMin = hist.calcMin();
        double rangeMax = hist.quantile(quantile);

        // To avoid a situation where we have a 0 range
        if (rangeMax == rangeMin) {
            rangeMax = rangeMin + 1;
        }

        changeVoxels(vb.any(), rangeMin, rangeMax);
    }

    private static void changeVoxels(VoxelBox<?> vb, double rangeMin, double rangeMax) {

        double rangeExtent = rangeMax - rangeMin;
        double rangeMult = 255 / rangeExtent;

        Extent e = vb.extent();
        for (int z = 0; z < e.getZ(); z++) {

            VoxelBuffer<?> bb = vb.getPixelsForPlane(z);

            int offset = 0;
            for (int y = 0; y < e.getY(); y++) {
                for (int x = 0; x < e.getX(); x++) {

                    double val = (double) bb.getInt(offset);

                    int stretched = roundAndClip((val - rangeMin) * rangeMult);

                    bb.putInt(offset, stretched);

                    offset++;
                }
            }
        }
    }

    /** Rounds a value up or down, and clips to ensure its in the range 0..255 inclusive */
    private static int roundAndClip(double value) {

        int rounded = (int) Math.round(value);

        if (rounded > 255) {
            return 255;
        }
        if (rounded < 0) {
            return 0;
        }

        return rounded;
    }

    public double getQuantile() {
        return quantile;
    }

    public void setQuantile(double quantile) {
        this.quantile = quantile;
    }
}
