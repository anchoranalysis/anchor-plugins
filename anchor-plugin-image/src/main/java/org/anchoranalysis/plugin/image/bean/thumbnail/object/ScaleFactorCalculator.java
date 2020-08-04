package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.scale.ScaleFactorUtilities;

/**
 * Helpers determine a scaling-factor for objects to fit in a certain-sized scene.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ScaleFactorCalculator {

    /**
     * Calculates a minimal scaling necessary so that each bounding-box can fit inside a certain sized
     * scene
     *
     * <p>In otherwords the largest dimension of any object, must still be able to fit inside the
     * corresponding dimension of the target scene.
     *
     * @param boundingBoxes a stream of bounding-boxes, each of which must fit inside {@code targetSize}
     * @param targetSize the size in which all bounding-boxes must fit
     * @return a scale-factor that can be applied to the bounding-boxes so that they will always fit inside
     *     {@code targetSize}
     */
    public static ScaleFactor scaleEachObjectFitsIn(Supplier<Stream<BoundingBox>> boundingBoxes, Extent targetSize) {
        Extent maxInEachDimension =
                new Extent(
                        extractMaxDimension(boundingBoxes.get(), Extent::x),
                        extractMaxDimension(boundingBoxes.get(), Extent::y));
        return ScaleFactorUtilities.calcRelativeScale(maxInEachDimension, targetSize);
    }

    private static int extractMaxDimension(
            Stream<BoundingBox> boundingBoxes, ToIntFunction<Extent> functionDimension) {
        return boundingBoxes.map( BoundingBox::extent).mapToInt(functionDimension).max().getAsInt();
    }
}
