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

/** A voxel score that can optionally normalize and adjust the intensity values. */
public class Identity extends VoxelScore {

    // START BEAN PROPERTIES
    /** The index of the energy channel to use for calculations. */
    @BeanField @Getter @Setter private int energyChannelIndex = 0;

    /** Whether to normalize the voxel intensity values. */
    @BeanField @Getter @Setter private boolean normalize = true;

    /** The factor to apply for values below 0.5 when normalizing. */
    @BeanField @Getter @Setter private double factorLow = 1.0;

    /** The factor to apply for values above 0.5 when normalizing. */
    @BeanField @Getter @Setter private double factorHigh = 1.0;

    /** Whether to keep extreme values (0 and 1) unchanged when normalizing. */
    @BeanField @Getter @Setter private boolean keepExtremes = false;
    // END BEAN PROPERTIES

    @Override
    public double calculate(int[] voxelIntensities) throws FeatureCalculationException {

        double pixelValue = voxelIntensities[energyChannelIndex];

        if (normalize) {
            double val = pixelValue / 255;

            if (keepExtremes) {
                if (val == 0.0) {
                    return val;
                }
                if (val == 1.0) {
                    return val;
                }
            }

            if (val < 0.5) {
                return 0.5 - ((0.5 - val) * factorLow);
            } else if (val > 0.5) {
                return 0.5 + ((val - 0.5) * factorHigh);
            } else {
                return val;
            }

        } else {
            return pixelValue;
        }
    }
}
