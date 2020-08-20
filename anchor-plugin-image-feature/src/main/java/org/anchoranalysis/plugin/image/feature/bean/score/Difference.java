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
import org.anchoranalysis.image.feature.bean.score.VoxelScore;

/**
 * The score is {@code 
 * @author Owen Feehan
 *
 */
public class Difference extends VoxelScore {

    // START BEAN PROPERTIES
    /** Index of first channel */
    @BeanField @Getter @Setter private int channelFirst = 0;

    /** Index of second channel */
    @BeanField @Getter @Setter private int channelSecond = 0;

    /** Width (what the difference is divided by) */
    @BeanField @Getter @Setter private double width = 10;

    /** A minimum difference, below which 0.0 is returned */
    @BeanField @Getter @Setter private int minDifference = 0;
    // END BEAN PROPERTIES
    
    @Override
    public double calculate(int[] pixelVals) throws FeatureCalculationException {
        return DifferenceHelper.differenceFromParams(
                pixelVals, channelFirst, channelSecond, width, minDifference);
    }
}
