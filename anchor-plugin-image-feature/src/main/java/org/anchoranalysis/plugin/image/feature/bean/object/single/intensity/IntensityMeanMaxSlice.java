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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/** Calculates the maximum mean intensity across all slices of an object in a channel. */
public class IntensityMeanMaxSlice extends FeatureEnergyChannel {

    /** If true, zero-valued voxels are excluded from the mean intensity calculation. */
    @BeanField @Getter @Setter private boolean excludeZero = false;

    /** The value to return when the object has no voxels in any slice. */
    @BeanField @Getter @Setter private int emptyValue = 0;

    /**
     * Calculates the maximum mean intensity across all slices of the object in the given channel.
     *
     * @param input the {@link FeatureCalculationInput} containing the {@link
     *     FeatureInputSingleObject}
     * @param channel the {@link Channel} to calculate the intensity from
     * @return the maximum mean intensity across all slices, or {@link #emptyValue} if the object
     *     has no voxels
     * @throws FeatureCalculationException if the calculation fails
     */
    @Override
    protected double calculateForChannel(
            FeatureCalculationInput<FeatureInputSingleObject> input, Channel channel)
            throws FeatureCalculationException {

        ValueAndIndex vai =
                StatsHelper.calculateMaxSliceMean(channel, input.get().getObject(), excludeZero);

        if (vai.getIndex() == -1) {
            return emptyValue;
        }

        return vai.getValue();
    }
}
