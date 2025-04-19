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

package org.anchoranalysis.plugin.image.bean.object.provider.morphological;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.WithOptionalDimensionsBase;
import org.anchoranalysis.spatial.box.Extent;

/** Base class for providers that apply morphological operations to an {@link ObjectCollection}. */
public abstract class ObjectCollectionProviderMorphological extends WithOptionalDimensionsBase {

    // START BEAN PROPERTIES
    /** Whether to perform the operation in 3D (true) or 2D (false). */
    @BeanField @Getter @Setter private boolean do3D = false;

    /** The number of iterations to apply the morphological operation. */
    @BeanField @Getter @Setter private int iterations = 1;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects)
            throws ProvisionFailedException {
        Optional<Extent> extent = deriveExtent();
        return objects.stream().map(objectMask -> applyMorphologicalOperation(objectMask, extent));
    }

    /**
     * Applies the morphological operation to a single {@link ObjectMask}.
     *
     * @param object the object to apply the operation to
     * @param extent optional extent to constrain the operation
     * @return the resulting object after applying the operation
     * @throws ProvisionFailedException if the operation fails
     */
    protected abstract ObjectMask applyMorphologicalOperation(
            ObjectMask object, Optional<Extent> extent) throws ProvisionFailedException;

    /**
     * Derives the extent from the dimensions, if available.
     *
     * @return an optional extent derived from the dimensions
     * @throws ProvisionFailedException if deriving the extent fails
     */
    protected Optional<Extent> deriveExtent() throws ProvisionFailedException {
        return createDims().map(Dimensions::extent);
    }
}
