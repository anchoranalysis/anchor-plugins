/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

/**
 * Considers all possible pairs of objects in a provider, and removes those that touch the border.
 *
 * @author Owen Feehan
 */
public class RemoveTouchingBorder extends ObjectCollectionProviderWithDimensions {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean useZ = true;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        ImageDimensions dimensions = createDimensions();

        return objects.stream().filter(objectMask -> !atBorder(objectMask, dimensions));
    }

    private boolean atBorder(ObjectMask object, ImageDimensions dim) {
        if (useZ) {
            return object.getBoundingBox().atBorder(dim);
        } else {
            return object.getBoundingBox().atBorderXY(dim);
        }
    }
}
