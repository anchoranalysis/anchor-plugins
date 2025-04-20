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

package org.anchoranalysis.plugin.image.feature.bean.stack.object;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Derives a {@link FeatureInputSingleObject} from a {@link FeatureInputStack} by extracting an
 * object mask from a specific energy channel.
 *
 * <p>This class extends {@link CalculationPart} to provide functionality for deriving a single
 * object input from a stack input.
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CalculateDeriveObjectInput
        extends CalculationPart<FeatureInputSingleObject, FeatureInputStack> {

    /** The index of the energy channel to extract the object mask from. */
    private final int energyIndex;

    @Override
    protected FeatureInputSingleObject execute(FeatureInputStack input)
            throws FeatureCalculationException {
        return new FeatureInputSingleObject(
                extractObjectMask(input), input.getEnergyStackOptional());
    }

    /**
     * Extracts an {@link ObjectMask} from the input stack's energy channel.
     *
     * @param input the {@link FeatureInputStack} to extract the object mask from
     * @return the extracted {@link ObjectMask}
     * @throws FeatureCalculationException if there's an error during extraction
     */
    private ObjectMask extractObjectMask(FeatureInputStack input)
            throws FeatureCalculationException {

        Mask mask = new Mask(input.getEnergyStackRequired().getChannel(energyIndex));
        return new ObjectMask(mask.binaryVoxels());
    }
}
