/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.channel.provider.gradient;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.axis.AxisTypeConverter;
import org.anchoranalysis.core.axis.AxisTypeException;
import org.anchoranalysis.core.error.CreateException;

/**
 * Extracts the gradient in the direction of a particular axis (identified by an index)
 *
 * @author Owen Feehan
 */
public class GradientForAxis extends GradientBaseAddSum {

    // START BEAN
    /** Which axis? X=0, Y=1, Z=2 */
    @BeanField @Getter @Setter private String axis = "x";
    // END BEAN

    @Override
    protected boolean[] createAxisArray() throws CreateException {
        switch (axisType()) {
            case X:
                return new boolean[] {true, false, false};
            case Y:
                return new boolean[] {false, true, false};
            case Z:
                return new boolean[] {false, false, true};
            default:
                throw new CreateException("Axis must be: x or y or z");
        }
    }

    private AxisType axisType() throws CreateException {
        try {
            return AxisTypeConverter.createFromString(axis);
        } catch (AxisTypeException e) {
            throw new CreateException(e.friendlyMessageHierarchy());
        }
    }
}
