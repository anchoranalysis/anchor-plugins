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
 * A score that is calculated on a single channel only.
 *
 * <p>This abstract class extends {@link VoxelScore} to provide a base for voxel scores that operate
 * on a single energy channel.
 *
 * @author Owen Feehan
 */
public abstract class SingleChannel extends VoxelScore {

    // START BEAN PROPERTIES
    /** The index of the energy channel to use for score calculation. */
    @BeanField @Getter @Setter private int energyChannelIndex = 0;

    // END BEAN PROPERTIES

    @Override
    public double calculate(int[] voxelIntensities) throws FeatureCalculationException {
        return deriveScoreFromVoxel(voxelIntensities[energyChannelIndex]);
    }

    /**
     * Derives a score from a single voxel intensity.
     *
     * @param voxelIntensity the intensity of the voxel in the specified energy channel
     * @return the calculated score for the voxel
     */
    protected abstract double deriveScoreFromVoxel(int voxelIntensity);
}
