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

package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.plugin.image.bean.dimensions.provider.GuessDimensions;

/**
 * Base class for {@link ObjectCollectionProviderUnary} classes that require dimensions to be
 * specified.
 *
 * @see WithOptionalDimensionsBase for a similar class with optional dimension specification.
 */
public abstract class WithDimensionsBase extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** Provider for the dimensions to be used. Defaults to {@link GuessDimensions}. */
    @BeanField @Getter @Setter private DimensionsProvider dimensions = new GuessDimensions();

    // END BEAN PROPERTIES

    /**
     * Creates the dimensions using the specified {@link DimensionsProvider}.
     *
     * @return the created dimensions
     * @throws ProvisionFailedException if creating the dimensions fails
     */
    protected Dimensions createDimensions() throws ProvisionFailedException {
        return dimensions.get();
    }
}
