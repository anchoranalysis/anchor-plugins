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
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/**
 * Base class for {@link ObjectCollectionProviderUnary} that take an optional {@code
 * objectsContainer} bean-field.
 */
public abstract class WithContainerBase extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** Optional provider for a container of objects. */
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsContainer;
    // END BEAN PROPERTIES

    /**
     * Retrieves the optional container of objects.
     *
     * @return an optional containing the object collection if present, or empty if not
     * @throws ProvisionFailedException if retrieving the container fails
     */
    protected Optional<ObjectCollection> containerOptional() throws ProvisionFailedException {
        if (objectsContainer != null) {
            return Optional.of(objectsContainer.get());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the required container of objects.
     *
     * @return the object collection
     * @throws ProvisionFailedException if the container is not defined or retrieving it fails
     */
    protected ObjectCollection containerRequired() throws ProvisionFailedException {
        return containerOptional()
                .orElseThrow(
                        () ->
                                new ProvisionFailedException(
                                        "An objects-container must be defined for this provider"));
    }
}
