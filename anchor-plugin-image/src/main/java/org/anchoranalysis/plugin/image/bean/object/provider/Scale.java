/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.scale.ScaleFactor;

/**
 * Scales all the objects in the collection by a particular scale-factor.
 *
 * @author Owen Feehan
 */
public class Scale extends ObjectCollectionProviderWithDimensions {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objectCollection)
            throws CreateException {

        ImageDimensions dimensions = createDimensions();

        ScaleFactor scaleFactor;
        try {
            scaleFactor = scaleCalculator.calc(dimensions);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }

        return objectCollection.scale(
                scaleFactor, InterpolatorFactory.getInstance().binaryResizing());
    }
}
