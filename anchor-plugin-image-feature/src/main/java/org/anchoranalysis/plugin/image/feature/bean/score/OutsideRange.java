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

package org.anchoranalysis.plugin.image.feature.bean.score;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.VoxelScore;

/**
 * Calculates a voxel score based on whether the voxel intensity is outside a specified range.
 *
 * <p>This class extends {@link VoxelScore} to provide a specific scoring mechanism for voxels.
 */
public class OutsideRange extends VoxelScore {

    // START BEAN PROPERTIES
    /** The minimum value of the range (inclusive). */
    @BeanField @Getter @Setter private int min = 0;

    /** The maximum value of the range (exclusive). */
    @BeanField @Getter @Setter private int max = 256;

    /** The index of the energy channel to use for intensity values. */
    @BeanField @Getter @Setter private int energyIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public double calculate(int[] voxelIntensities) throws FeatureCalculationException {

        double value = voxelIntensities[energyIndex];
        if (value < min) {
            return 0;
        }
        if (value > max) {
            return 255;
        }
        return value;
    }
}
