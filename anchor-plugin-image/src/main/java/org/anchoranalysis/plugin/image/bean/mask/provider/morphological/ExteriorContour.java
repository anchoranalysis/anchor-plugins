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

package org.anchoranalysis.plugin.image.bean.mask.provider.morphological;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.core.contour.FindContour;
import org.anchoranalysis.image.core.mask.Mask;

/**
 * Finds the exterior <i>outer</i> contour for a {@link Mask} that forms its outline.
 *
 * <p>It always creates a new mask for the result, without consuming the input.
 *
 * @author Owen Feehan
 */
public class ExteriorContour extends MaskProviderUnary {

    // START BEAN PROPERTIES
    /**
     * If true, any 3D mask is flattened in the z-dimension (maximum intensity projection) to make
     * it 2D
     */
    @BeanField @Getter @Setter private boolean flatten = false;

    @BeanField @Getter @Setter private boolean outlineAtBoundary = true;

    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask mask) throws ProvisionFailedException {
        return FindContour.createFromGuess3D(mask, 1, flatten, outlineAtBoundary);
    }
}
