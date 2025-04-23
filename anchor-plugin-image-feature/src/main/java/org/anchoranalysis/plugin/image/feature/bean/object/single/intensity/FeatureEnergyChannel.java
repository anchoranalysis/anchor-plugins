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
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/**
 * A feature that uses a channel from the energy-stack as identified by an index.
 *
 * @author Owen Feehan
 */
public abstract class FeatureEnergyChannel extends FeatureSingleObject {

    /** Index of channel to use in the energy-stack (0 is the first channel, 1 is second etc.) */
    @BeanField @Getter @Setter private int energyIndex = 0;

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        Channel channel = input.get().getEnergyStackRequired().getChannel(energyIndex);
        return calculateForChannel(input, channel);
    }

    /**
     * Calculates the feature value for a specific channel in the energy stack.
     *
     * @param input the {@link FeatureCalculationInput} containing the {@link
     *     FeatureInputSingleObject}
     * @param channel the {@link Channel} from the energy stack to calculate the feature on
     * @return the calculated feature value
     * @throws FeatureCalculationException if the calculation fails
     */
    protected abstract double calculateForChannel(
            FeatureCalculationInput<FeatureInputSingleObject> input, Channel channel)
            throws FeatureCalculationException;
}
