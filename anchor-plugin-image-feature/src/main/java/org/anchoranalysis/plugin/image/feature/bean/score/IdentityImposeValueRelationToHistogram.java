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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.bean.score.VoxelScore;
import org.anchoranalysis.image.histogram.Histogram;

public class IdentityImposeValueRelationToHistogram extends VoxelScore {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int energyChannelIndexCheck = 0;

    @BeanField @Getter @Setter private int energyChannelIndexFail = 0;

    @BeanField @Getter @Setter private int histIndex = 0;

    @BeanField @Getter @Setter private RelationBean relation;

    @BeanField @Getter @Setter private double value = 0;

    @BeanField @Getter @Setter private boolean max = true; // We use the max, otherwise the min
    // END BEAN PROPERTIES

    private int histMax;

    @Override
    public double calculate(int[] pixelVals) throws FeatureCalculationException {

        double pxlValue = pixelVals[energyChannelIndexCheck];

        if (relation.create().isRelationToValueTrue(pxlValue, histMax)) {
            return value;
        } else {
            return pixelVals[energyChannelIndexFail];
        }
    }

    @Override
    public void init(List<Histogram> histograms, Optional<KeyValueParams> keyValueParams)
            throws InitException {
        try {
            if (max) {
                histMax = histograms.get(histIndex).calculateMaximum();
            } else {
                histMax = histograms.get(histIndex).calculateMinimum();
            }
        } catch (OperationFailedException e) {
            throw new InitException(e);
        }
    }
}
