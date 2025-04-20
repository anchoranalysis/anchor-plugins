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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.math.histogram.Histogram;

/** Base class for calculating a score based on a threshold level derived from a histogram. */
public abstract class CalculateLevelBase extends SingleChannel {

    // START BEAN PROPERTIES
    /** The method to calculate the threshold level. */
    @BeanField @Getter @Setter private CalculateLevel calculateLevel;

    /** The index of the histogram channel to use for calculations. */
    @BeanField @Getter @Setter private int histogramChannelIndex = 0;
    // END BEAN PROPERTIES

    /** The calculated threshold level. */
    private int level;

    @Override
    public void initialize(List<Histogram> histograms, Optional<Dictionary> dictionary)
            throws InitializeException {

        try {
            Histogram histogram = histograms.get(histogramChannelIndex);
            level = calculateLevel.calculateLevel(histogram);

            beforeCalcSetup(histogram, level);
        } catch (OperationFailedException e) {
            throw new InitializeException(e);
        }
    }

    @Override
    protected double deriveScoreFromVoxel(int voxelIntensity) {
        return calculateForVoxel(voxelIntensity, level);
    }

    /**
     * Performs setup operations before calculation.
     *
     * @param histogram the {@link Histogram} to use for setup
     * @param level the calculated threshold level
     * @throws OperationFailedException if the setup operation fails
     */
    protected abstract void beforeCalcSetup(Histogram histogram, int level)
            throws OperationFailedException;

    /**
     * Calculates a score for a single voxel based on its intensity and the threshold level.
     *
     * @param voxelIntensity the intensity of the voxel
     * @param level the threshold level
     * @return a score for the voxel
     */
    protected abstract double calculateForVoxel(int voxelIntensity, int level);
}
