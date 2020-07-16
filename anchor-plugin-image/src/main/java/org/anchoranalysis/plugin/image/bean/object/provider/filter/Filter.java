/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;

public class Filter extends ObjectCollectionProviderFilterBase {

    @Override
    protected ObjectCollection createFromObjects(
            ObjectCollection objects,
            Optional<ObjectCollection> objectsRejected,
            Optional<ImageDimensions> dim)
            throws CreateException {
        return filter(objects, dim, objectsRejected);
    }
}
