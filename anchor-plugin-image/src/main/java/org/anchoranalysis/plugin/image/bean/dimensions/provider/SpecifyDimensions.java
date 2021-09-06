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
package org.anchoranalysis.plugin.image.bean.dimensions.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.spatial.Extent;

/**
 * Specify dimensions via a bean.
 *
 * <p>No resolution information is assigned.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class SpecifyDimensions extends DimensionsProvider {

    // START BEAN PROPERTIES
    /** Length of X and Y dimensions. */
    @BeanField @Getter @Setter SizeXY sizeXY;

    /** Length of Z dimension. */
    @BeanField @Getter @Setter int sizeZ = 1;
    // END BEAN PROPERTIES

    /**
     * Create with specific lengths for X and Y dimensions.
     *
     * @param sizeXY the size
     */
    public SpecifyDimensions(SizeXY sizeXY) {
        this.sizeXY = sizeXY;
    }

    @Override
    public Dimensions get() {
        Extent extent = sizeXY.asExtent(sizeZ);
        return new Dimensions(extent, Optional.empty());
    }
}
