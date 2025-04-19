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

package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.WithOptionalDimensionsBase;

/** Base class for object collection providers that apply a filter to the objects. */
public abstract class ObjectCollectionProviderFilterBase extends WithOptionalDimensionsBase {

    // START BEAN PROPERTIES
    /** The filter to apply to the object collection. */
    @BeanField @Getter @Setter private ObjectFilter filter;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects)
            throws ProvisionFailedException {
        return createFromObjects(objects, Optional.empty(), createDims());
    }

    /**
     * Filters the given object collection using the specified filter.
     *
     * @param objects the {@link ObjectCollection} to filter
     * @param dimensions optional {@link Dimensions} of the objects
     * @param objectsRejected optional list to store rejected {@link ObjectMask}s
     * @return the filtered {@link ObjectCollection}
     * @throws ProvisionFailedException if filtering fails
     */
    protected ObjectCollection filter(
            ObjectCollection objects,
            Optional<Dimensions> dimensions,
            Optional<List<ObjectMask>> objectsRejected)
            throws ProvisionFailedException {
        try {
            return filter.filter(objects, dimensions);
        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    /**
     * Creates an {@link ObjectCollection} from the given objects, potentially applying a filter and
     * considering dimensions.
     *
     * @param objects the input {@link ObjectCollection}
     * @param objectsRejected optional list to store rejected {@link ObjectMask}s
     * @param dimensions optional {@link Dimensions} of the objects
     * @return the created {@link ObjectCollection}
     * @throws ProvisionFailedException if creation fails
     */
    protected abstract ObjectCollection createFromObjects(
            ObjectCollection objects,
            Optional<List<ObjectMask>> objectsRejected,
            Optional<Dimensions> dimensions)
            throws ProvisionFailedException;
}
