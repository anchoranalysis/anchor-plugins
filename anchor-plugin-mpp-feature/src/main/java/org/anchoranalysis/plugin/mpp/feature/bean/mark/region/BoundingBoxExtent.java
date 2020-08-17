/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.mark.region;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.core.axis.AxisTypeException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.plugin.mpp.feature.bean.unit.UnitConverter;

public class BoundingBoxExtent extends FeatureMarkRegion {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String axis = "x";

    @BeanField @Getter @Setter private UnitConverter unit = new UnitConverter();
    // END BEAN PARAMETERS

    @Override
    public double calculate(SessionInput<FeatureInputMark> input) throws FeatureCalculationException {

        ImageDimensions dimensions = input.get().getDimensionsRequired();

        BoundingBox box = input.get().getMark().box(dimensions, getRegionID());

        try {
            return resolveDistance(
                    box,
                    Optional.of(dimensions.resolution()),
                    AxisTypeConverter.createFromString(axis));
        } catch (AxisTypeException e) {
            throw new FeatureCalculationException(e);
        }
    }

    @Override
    public String describeParams() {
        return String.format("%s", axis);
    }

    private double resolveDistance(
            BoundingBox box, Optional<ImageResolution> res, AxisType axisType)
            throws FeatureCalculationException {
        try {
            return unit.resolveDistance(
                    box.extent().valueByDimension(axisType),
                    res,
                    unitVector(AxisTypeConverter.dimensionIndexFor(axisType)));
        } catch (AxisTypeException e) {
            throw new FeatureCalculationException(e.friendlyMessageHierarchy());
        }
    }

    private DirectionVector unitVector(int dimIndex) {
        DirectionVector dirVector = new DirectionVector();
        dirVector.setIndex(dimIndex, 1);
        return dirVector;
    }
}
