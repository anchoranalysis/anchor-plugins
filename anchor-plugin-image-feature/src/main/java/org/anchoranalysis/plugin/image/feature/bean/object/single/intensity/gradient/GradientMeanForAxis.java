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

package org.anchoranalysis.plugin.image.feature.bean.object.single.intensity.gradient;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.core.axis.AxisTypeException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Calculates the mean of the intensity-gradient defined by multiple Energy channels in a particular
 * direction
 *
 * <p>An Energy channel is present for X, Y and optionally Z intensity-gradients.
 *
 * <p>A constant is subtracted from the Energy channel (all positive) to center around 0
 *
 * @author Owen Feehan
 */
public class GradientMeanForAxis extends IntensityGradientBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String axis = "";
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        try {
            AxisType axisType = AxisTypeConverter.createFromString(axis);

            List<Point3d> points = input.calculate(gradientCalculation());

            double sum =
                    points.stream().mapToDouble(point -> point.valueByDimension(axisType)).sum();
            return sum / points.size();

        } catch (AxisTypeException e) {
            throw new FeatureCalculationException(e.friendlyMessageHierarchy());
        }
    }
}
