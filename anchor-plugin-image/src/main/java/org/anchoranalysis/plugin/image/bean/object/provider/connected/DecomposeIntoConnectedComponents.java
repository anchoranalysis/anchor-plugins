/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.connected;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;

/**
 * Ensures each object in a collection is a connected-component, decomposing it if necessary into
 * multiple objects.
 *
 * @author Owen Feehan
 */
public class DecomposeIntoConnectedComponents extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** if TRUE, uses 8 neighborhood instead of 4, and similarly in 3d */
    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        CreateFromConnectedComponentsFactory creator =
                new CreateFromConnectedComponentsFactory(bigNeighborhood, 1);

        return objects.stream()
                .flatMapWithException(
                        CreateException.class, objectMask -> createObjects3D(objectMask, creator));
    }

    private ObjectCollection createObjects3D(
            ObjectMask unconnected, CreateFromConnectedComponentsFactory createObjectMasks)
            throws CreateException {

        ObjectCollection objects =
                createObjectMasks.createConnectedComponents(unconnected.binaryVoxelBox());

        // Adjust the crnr of each object, by adding on the original starting point of our
        // object-mask
        return objects.shiftBy(unconnected.getBoundingBox().cornerMin());
    }
}
