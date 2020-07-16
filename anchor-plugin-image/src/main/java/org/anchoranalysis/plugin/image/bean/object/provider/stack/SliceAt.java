/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.stack;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.object.ObjectCollection;

public class SliceAt extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    /** Index in z-dimension of slice to extract */
    @BeanField @Getter @Setter private int index = 0;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {
        return objects.stream()
                .filterAndMap(
                        objectMask -> objectMask.getBoundingBox().contains().z(index),
                        objectMask ->
                                objectMask.extractSlice(
                                        index - objectMask.getBoundingBox().cornerMin().getZ(),
                                        false));
    }
}
