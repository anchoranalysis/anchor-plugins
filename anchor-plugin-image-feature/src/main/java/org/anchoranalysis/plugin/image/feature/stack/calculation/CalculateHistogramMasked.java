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

package org.anchoranalysis.plugin.image.feature.stack.calculation;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.math.histogram.Histogram;

/**
 * Calculated a histogram for a specific region on a channel, as identified by a mask in another
 * channel
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateHistogramMasked extends FeatureCalculation<Histogram, FeatureInputStack> {

    /** the index in the energy-stack of the channel part of whose signal will form a histogram */
    private final int energyIndexSignal;

    /** the index in the energy-stack of a channel which is a binary mask (0=off, 255=on) */
    private final int energyIndexMask;

    @Override
    protected Histogram execute(FeatureInputStack input) throws FeatureCalculationException {

        try {
            EnergyStackWithoutParams energyStack = input.getEnergyStackRequired().withoutParams();

            return HistogramFromObjectsFactory.create(
                    extractChannel(energyStack), extractMask(energyStack));

        } catch (CreateException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private Channel extractChannel(EnergyStackWithoutParams energyStack) {
        return energyStack.getChannel(energyIndexSignal);
    }

    private Mask extractMask(EnergyStackWithoutParams energyStack) {
        return new Mask(energyStack.getChannel(energyIndexMask));
    }
}
