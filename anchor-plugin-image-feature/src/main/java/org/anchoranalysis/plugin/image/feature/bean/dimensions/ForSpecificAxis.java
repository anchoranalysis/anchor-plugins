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

package org.anchoranalysis.plugin.image.feature.bean.dimensions;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.spatial.axis.AxisType;
import org.anchoranalysis.spatial.axis.AxisTypeConverter;
import org.anchoranalysis.spatial.axis.AxisTypeException;

/**
 * Dimensions-calculation for one specific axis only.
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public abstract class ForSpecificAxis<T extends FeatureInputEnergy> extends FromDimensionsBase<T> {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private String axis = "x";
    // END BEAN PARAMETERS

    @Override
    protected double calculateFromDimensions(Dimensions dim) throws FeatureCalculationException {
        try {
            return calculateForAxis(dim, AxisTypeConverter.createFromString(axis));
        } catch (AxisTypeException e) {
            throw new FeatureCalculationException(e.friendlyMessageHierarchy());
        }
    }

    protected abstract double calculateForAxis(Dimensions dimensions, AxisType axis)
            throws FeatureCalculationException;

    @Override
    public String describeParams() {
        return String.format("%s", axis);
    }
}
