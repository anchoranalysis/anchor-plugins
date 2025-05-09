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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.core.dimensions.Dimensions;

/**
 * Base class for {@link ObjectCollectionProviderUnary} classes that offer an optional means to
 * specify dimensions.
 *
 * @see WithDimensionsBase for a similar class with mandatory dimension specification.
 * @author Owen Feehan
 */
public abstract class WithOptionalDimensionsBase extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** Optional provider for dimensions. */
    @BeanField @OptionalBean @Getter @Setter private DimensionsProvider dimensions;

    // END BEAN PROPERTIES

    /**
     * Creates the dimensions if a {@link DimensionsProvider} is specified.
     *
     * @return an optional containing the dimensions if provided, or empty if not
     * @throws ProvisionFailedException if creating the dimensions fails
     */
    protected Optional<Dimensions> createDims() throws ProvisionFailedException {
        if (dimensions != null) {
            return Optional.of(dimensions.get());
        } else {
            return Optional.empty();
        }
    }
}
