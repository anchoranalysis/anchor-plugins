/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.stack;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Flattens an object-mask in the z-dimension, so it is 2D instead of 3D (like a maximum intensity
 * projection).
 *
 * <p>2D objects are unchanged.
 *
 * @author Owen Feehan
 */
public class FlattenZ extends ObjectCollectionProviderUnary {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
        return objects.stream().map(ObjectMask::flattenZ);
    }
}
