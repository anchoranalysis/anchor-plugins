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
import org.anchoranalysis.bean.shared.relation.GreaterThanEqualToBean;
import org.anchoranalysis.bean.shared.relation.LessThanBean;
import org.anchoranalysis.bean.shared.relation.threshold.RelationToConstant;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.histogram.Histogram;

/**
 * Similar to {@link Difference} but calculates the width as the standard-deviation of the histogram
 *
 * @author Owen Feehan
 */
public class DifferenceCalculateLevelStandardDeviation extends CalculateLevelBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int minDifference = 0;

    @BeanField @Getter @Setter private double widthFactor = 1.0;
    // END BEAN PROPERTIES

    private double widthLessThan;
    private double widthGreaterThan;

    @Override
    protected void beforeCalcSetup(Histogram histogram, int level) throws OperationFailedException {

        Histogram lessThan = histogram.threshold(new RelationToConstant(new LessThanBean(), level));
        Histogram greaterThan =
                histogram.threshold(new RelationToConstant(new GreaterThanEqualToBean(), level));

        this.widthLessThan = lessThan.standardDeviation() * widthFactor;
        this.widthGreaterThan = greaterThan.standardDeviation() * widthFactor;
    }

    @Override
    protected double calculateForVoxel(int voxelIntensity, int level) {
        return DifferenceHelper.differenceFromValue(
                voxelIntensity, level, widthGreaterThan, widthLessThan, minDifference);
    }
}
