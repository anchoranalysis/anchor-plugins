/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithOptionalDimensions;

public abstract class ObjectCollectionProviderFilterBase
        extends ObjectCollectionProviderWithOptionalDimensions {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectFilter filter;

    @BeanField @OptionalBean @Getter @Setter
    private ObjectCollectionProvider
            objectsRejected; // The rejected objects are put here (OPTIONAL)
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
        return createFromObjects(objects, OptionalFactory.create(objectsRejected), createDims());
    }

    protected ObjectCollection filter(
            ObjectCollection objects,
            Optional<ImageDimensions> dim,
            Optional<ObjectCollection> objectsRejected)
            throws CreateException {
        try {
            return filter.filter(objects, dim, objectsRejected);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    protected abstract ObjectCollection createFromObjects(
            ObjectCollection objects,
            Optional<ObjectCollection> objectsRejected,
            Optional<ImageDimensions> dim)
            throws CreateException;
}
