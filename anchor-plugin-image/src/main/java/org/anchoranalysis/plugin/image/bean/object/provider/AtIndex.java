/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;

/**
 * Creates a new object-collection with only a single element (at a particular index) in the
 * existing collection.
 *
 * @author Owen Feehan
 */
public class AtIndex extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int index = 0;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        if (index >= objects.size()) {
            throw new CreateException(
                    String.format(
                            "Index %d is out of bounds. Object-Collection has %d items",
                            index, objects.size()));
        }

        return ObjectCollectionFactory.from(objects.get(index));
    }
}
