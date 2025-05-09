/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.bean.fitter;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitter;

/** Base class for conic fitters, providing common properties and methods. */
public abstract class ConicFitterBase extends PointsFitter {

    // START BEAN
    /** The shell thickness of the conic. */
    @BeanField @Positive @Getter @Setter private double shell;

    /** The value to subtract from the radii of the conic. */
    @BeanField @Getter @Setter private double subtractRadii = 0.0;

    /** The scale factor to apply to the radii of the conic. */
    @BeanField @Getter @Setter private double scaleRadii = 1.0;

    /** The shift to apply to input points before fitting. */
    @BeanField @Getter @Setter private float inputPointShift = 0.0f;

    // END BEAN

    /**
     * Copies fields in this (base) class to {@code target}.
     *
     * <p>This is intended to be called by sub-classes to help when duplicating.
     *
     * @param target the object fields are assigned to.
     */
    protected void assignTo(ConicFitterBase target) {
        target.shell = shell;
        target.subtractRadii = subtractRadii;
        target.inputPointShift = inputPointShift;
        target.scaleRadii = scaleRadii;
    }
}
