/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.plugin.opencv.nonmaxima.WithConfidence;

/**
 * Extracts and object-mask from the list and scales
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ScaleExtractObjects {

    public static ObjectCollection apply(
            List<WithConfidence<ObjectMask>> list, ScaleFactor scaleFactor) {
        // Scale back to the needed original resolution
        return scaleObjects(extractObjects(list), scaleFactor);
    }

    private static ObjectCollection extractObjects(List<WithConfidence<ObjectMask>> list) {
        return ObjectCollectionFactory.mapFrom(list, WithConfidence::getObject);
    }

    private static ObjectCollection scaleObjects(
            ObjectCollection objects, ScaleFactor scaleFactor) {
        return objects.scale(scaleFactor, InterpolatorFactory.getInstance().binaryResizing());
    }
}
