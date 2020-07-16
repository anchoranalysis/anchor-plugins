/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * Duplicates (deep copy) an object-collection.
 *
 * @author Owen Feehan
 */
public class Duplicate extends ObjectCollectionProviderUnary {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
        return objects.duplicate();
    }
}
