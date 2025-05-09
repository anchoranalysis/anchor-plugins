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

package org.anchoranalysis.plugin.points.bean.convexhull;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.core.contour.FindContour;
import org.anchoranalysis.image.core.mask.Mask;

/** Base class for convex hull implementations. */
public abstract class ConvexHullBase extends MaskProviderUnary {

    // START BEAN PROPERTIES
    /** Whether to erode the mask at the boundary before finding the contour. */
    @BeanField @Getter @Setter private boolean erodeAtBoundary = false;

    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws ProvisionFailedException {
        return createFromMask(mask, FindContour.createFrom(mask, 1, true, erodeAtBoundary));
    }

    /**
     * Creates a convex hull from a mask and its outline.
     *
     * @param mask the input {@link Mask}
     * @param outline the outline {@link Mask} of the input mask
     * @return the convex hull {@link Mask}
     * @throws ProvisionFailedException if the convex hull creation fails
     */
    protected abstract Mask createFromMask(Mask mask, Mask outline) throws ProvisionFailedException;
}
