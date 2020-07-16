/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

/**
 * Creates an empty object-collection.
 *
 * @author Owen Feehan
 */
public class Empty extends ObjectCollectionProvider {

    @Override
    public ObjectCollection create() throws CreateException {
        return ObjectCollectionFactory.empty();
    }
}
