package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.function.ToIntFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

/**
 * Helpers determine a scaling-factor for objects to fit in a certain-sized scene.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ObjectScalingHelper {

    /**
     * Calculates a minimal scaling necessary so that each object can fit inside a certain sized
     * scene
     *
     * <p>In otherwords the largest dimension of any object, must still be able to fit inside the
     * corresponding dimension of the target scene.
     *
     * @param objects a collection of objects, each of which must fit inside {@code targetSize}
     * @param targetSize the size in which all objects must fit
     * @return a scale-factor that can be applied to the objects so that they will always fit inside
     *     {@code targetSize}
     */
    public static ScaleFactor scaleEachObjectFitsIn(ObjectCollection objects, Extent targetSize) {
        Extent maxInEachDimension =
                new Extent(
                        extractMaxDimension(objects, Extent::getX),
                        extractMaxDimension(objects, Extent::getY));
        return ScaleFactorUtilities.calcRelativeScale(maxInEachDimension, targetSize);
    }

    private static int extractMaxDimension(
            ObjectCollection objects, ToIntFunction<Extent> functionDimension) {
        return objects.stream()
                .maxAsInt(object -> functionDimension.applyAsInt(object.getBoundingBox().extent()))
                .getAsInt();
    }
}
