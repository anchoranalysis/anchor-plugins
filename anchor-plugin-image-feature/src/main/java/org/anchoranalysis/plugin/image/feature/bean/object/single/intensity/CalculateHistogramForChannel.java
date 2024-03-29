/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.feature.input.FeatureInputHistogram;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.math.histogram.Histogram;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateHistogramForChannel
        extends CalculationPart<FeatureInputHistogram, FeatureInputSingleObject> {

    /**
     * iff true zero-intensity values are excluded from the histogram, otherwise they are included
     */
    private boolean excludeZero = false;

    /** an index uniquely identifying the channel */
    private int energyIndex;

    /** the channel corresponding to energyIndex */
    @EqualsAndHashCode.Exclude private Channel channel;

    @Override
    protected FeatureInputHistogram execute(FeatureInputSingleObject input) {

        Histogram histogram = HistogramFromObjectsFactory.createFrom(channel, input.getObject());

        if (excludeZero) {
            histogram.zeroValue(0);
        }

        return new FeatureInputHistogram(histogram, input.getResolutionOptional());
    }
}
