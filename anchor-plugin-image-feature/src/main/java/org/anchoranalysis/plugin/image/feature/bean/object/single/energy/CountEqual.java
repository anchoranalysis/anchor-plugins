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

package org.anchoranalysis.plugin.image.feature.bean.object.single.energy;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/** Counts the number of voxels in an object that have a specific intensity value in a channel. */
public class CountEqual extends SpecificEnergyChannelBase {

    /** The intensity value to count. Defaults to the 'on' value of {@link BinaryValuesInt}. */
    @BeanField @Getter @Setter private int value = BinaryValuesInt.getDefault().getOn();

    /**
     * Calculates the number of voxels in the object that have the specified intensity value in the
     * channel.
     *
     * @param object the {@link ObjectMask} defining the region of interest
     * @param channel the {@link Channel} to analyze
     * @return the count of voxels with the specified intensity value
     * @throws FeatureCalculationException if the calculation fails
     */
    @Override
    protected double calculateWithChannel(ObjectMask object, Channel channel)
            throws FeatureCalculationException {
        return channel.voxelsEqualTo(value).countForObject(object);
    }
}
