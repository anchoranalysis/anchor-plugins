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
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.VoxelScore;
import org.anchoranalysis.math.histogram.Histogram;

/** A voxel score that imposes a value relation to a histogram. */
public class IdentityImposeValueRelationToHistogram extends VoxelScore {

    // START BEAN PROPERTIES
    /** The index of the energy channel to check. */
    @BeanField @Getter @Setter private int energyChannelIndexCheck = 0;

    /** The index of the energy channel to use if the relation check fails. */
    @BeanField @Getter @Setter private int energyChannelIndexFail = 0;

    /** The index of the histogram to use for comparison. */
    @BeanField @Getter @Setter private int histogramIndex = 0;

    /** The relation to test between the voxel value and the histogram value. */
    @BeanField @Getter @Setter private RelationBean relation;

    /** The value to return if the relation test passes. */
    @BeanField @Getter @Setter private double value = 0;

    /** Whether to use the maximum (true) or minimum (false) value from the histogram. */
    @BeanField @Getter @Setter private boolean max = true;

    // END BEAN PROPERTIES

    /** The maximum or minimum value from the histogram, depending on the {@code max} property. */
    private int histogramMax;

    @Override
    public double calculate(int[] voxelIntensities) throws FeatureCalculationException {

        double pixelValue = voxelIntensities[energyChannelIndexCheck];

        if (relation.create().test(pixelValue, histogramMax)) {
            return value;
        } else {
            return voxelIntensities[energyChannelIndexFail];
        }
    }

    @Override
    public void initialize(List<Histogram> histograms, Optional<Dictionary> dictionary)
            throws InitializeException {
        try {
            if (max) {
                histogramMax = histograms.get(histogramIndex).calculateMaximum();
            } else {
                histogramMax = histograms.get(histogramIndex).calculateMinimum();
            }
        } catch (OperationFailedException e) {
            throw new InitializeException(e);
        }
    }
}
