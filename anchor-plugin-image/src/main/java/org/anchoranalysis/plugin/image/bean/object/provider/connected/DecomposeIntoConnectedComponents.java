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

package org.anchoranalysis.plugin.image.bean.object.provider.connected;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.voxel.binary.connected.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Ensures each object in a collection is a connected-component, decomposing it if necessary into
 * multiple objects.
 *
 * @author Owen Feehan
 */
public class DecomposeIntoConnectedComponents extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** if true, uses 8 neighborhood instead of 4, and similarly in 3d */
    @BeanField @Getter @Setter private boolean bigNeighborhood = false;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects)
            throws ProvisionFailedException {

        ObjectsFromConnectedComponentsFactory creator =
                new ObjectsFromConnectedComponentsFactory(bigNeighborhood, 1);

        return objects.stream()
                .flatMap(CreateException.class, objectMask -> createObjects3D(objectMask, creator));
    }

    private ObjectCollection createObjects3D(
            ObjectMask unconnected, ObjectsFromConnectedComponentsFactory createObjectMasks) {

        ObjectCollection objects = createObjectMasks.createUnsignedByte(unconnected.binaryVoxels());

        // Adjust the corner of each object, by adding on the original starting point of our
        // object-mask
        return objects.shiftBy(unconnected.boundingBox().cornerMin());
    }
}
