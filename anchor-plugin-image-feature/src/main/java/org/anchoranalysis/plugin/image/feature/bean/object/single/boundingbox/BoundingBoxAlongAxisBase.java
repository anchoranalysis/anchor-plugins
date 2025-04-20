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

package org.anchoranalysis.plugin.image.feature.bean.object.single.boundingbox;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureCalculationInput;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.spatial.axis.AxisConversionException;
import org.anchoranalysis.spatial.axis.AxisConverter;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

/**
 * Base class for features that calculate a value along a specific axis of an object's bounding box.
 */
public abstract class BoundingBoxAlongAxisBase extends FeatureSingleObject {

    // START BEAN PARAMETERS
    /** The axis along which to calculate the feature value ("x", "y", or "z"). */
    @BeanField @Getter @Setter private String axis = "x";
    // END BEAN PARAMETERS

    @Override
    public double calculate(FeatureCalculationInput<FeatureInputSingleObject> input)
            throws FeatureCalculationException {

        FeatureInputSingleObject inputSessionless = input.get();

        ReadableTuple3i point =
                extractTupleForBoundingBox(inputSessionless.getObject().boundingBox());

        return calculateAxisValue(point);
    }

    /**
     * Extracts a {@link ReadableTuple3i} from the given {@link BoundingBox}.
     *
     * @param box the {@link BoundingBox} to extract from
     * @return the extracted {@link ReadableTuple3i}
     */
    protected abstract ReadableTuple3i extractTupleForBoundingBox(BoundingBox box);

    @Override
    public String describeParameters() {
        return String.format("%s", axis);
    }

    /**
     * Calculates the value along the specified axis for the given point.
     *
     * @param point the point to calculate the value for
     * @return the calculated value
     * @throws FeatureCalculationException if an error occurs during calculation
     */
    private double calculateAxisValue(ReadableTuple3i point) throws FeatureCalculationException {
        try {
            return point.valueByDimension(AxisConverter.createFromString(axis));
        } catch (AxisConversionException e) {
            throw new FeatureCalculationException(e.friendlyMessageHierarchy());
        }
    }
}
