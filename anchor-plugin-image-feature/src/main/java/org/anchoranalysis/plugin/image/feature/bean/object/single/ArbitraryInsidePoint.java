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

package org.anchoranalysis.plugin.image.feature.bean.object.single;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.spatial.axis.Axis;
import org.anchoranalysis.spatial.axis.AxisConversionException;
import org.anchoranalysis.spatial.axis.AxisConverter;
import org.anchoranalysis.spatial.point.Point3i;

/**
 * Calculates deterministically a point that is definitely inside the object-mask and outputs a
 * selected axis value.
 *
 * <p>This feature finds an arbitrary point inside the object-mask and returns the coordinate value
 * for the specified axis.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class ArbitraryInsidePoint extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** The axis to output. Can be "x", "y", or "z". */
    @BeanField @Getter @Setter private String axis = "x";

    /** The value to return if no point is found inside the object-mask. */
    @BeanField @Getter @Setter private double emptyValue = 0;

    // END BEAN PROPERTIES

    /**
     * Creates an ArbitraryInsidePoint with a specified axis.
     *
     * @param axis the axis to output ("x", "y", or "z")
     */
    public ArbitraryInsidePoint(String axis) {
        this.axis = axis;
    }

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        try {
            Axis axisObj = AxisConverter.createFromString(axis);

            Optional<Point3i> arbPoint = input.get().getObject().findArbitraryOnVoxel();
            return arbPoint.map(point -> (double) point.valueByDimension(axisObj))
                    .orElse(emptyValue);

        } catch (AxisConversionException e) {
            throw new FeatureCalculationException(e.friendlyMessageHierarchy());
        }
    }
}
