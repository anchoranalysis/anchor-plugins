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

package org.anchoranalysis.plugin.image.bean.channel.provider.intensity;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsAll;
import org.anchoranalysis.image.voxel.statistics.HistogramFactory;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Changes the voxel values to map the range of 0th quantile to xth quantile across the entire voxel
 * data range
 *
 * @author Owen Feehan
 */
public class QuantileStretch extends ChannelProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double quantile = 1.0;
    // END BEAN PROPERTIES

    @Override
    public Channel createFromChannel(Channel channel) throws CreateException {
        try {
            histogramStretch(channel, quantile);
            return channel;
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private static void histogramStretch(Channel channel, double quantile)
            throws OperationFailedException {

        VoxelsWrapper voxels = channel.voxels();

        Histogram histogram = HistogramFactory.create(voxels);

        double rangeMin = histogram.calculateMinimum();
        double rangeMax = histogram.quantile(quantile);

        // To avoid a situation where we have a 0 range
        if (rangeMax == rangeMin) {
            rangeMax = rangeMin + 1;
        }

        changeVoxels(voxels.any(), rangeMin, rangeMax);
    }

    private static void changeVoxels(Voxels<?> voxels, double rangeMin, double rangeMax) {

        double rangeMult = 255 / (rangeMax - rangeMin);

        IterateVoxelsAll.changeIntensity(
                voxels, value -> roundAndClip((value - rangeMin) * rangeMult));
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
}
