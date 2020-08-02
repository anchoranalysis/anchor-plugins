package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import org.anchoranalysis.image.bean.size.Padding;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Adds optional padding to objects before being passed into another generator
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class AddPaddingToGenerator {

    public static IterableGenerator<ObjectMask> addPadding(IterableGenerator<ObjectMask> generator, ImageDimensions dimensions, Padding padding, boolean keepEntireImage) {
        // Maybe we need to change the objectMask to a padded version
        return new IterableGeneratorBridge<>(
            generator,
            object -> {
                if (keepEntireImage) {
                    return extractObjectKeepEntireImage(object, dimensions);
                } else {
                    return maybePadObject(object, dimensions, padding);
                }
            });
    }
    
    /**
     * Adds padding (if set) to an object-mask
     *
     * @param object object-mask to be padded
     * @param dimensions size of image
     * @return either the exist object-mask (if no padding is to be added) or a padded object-mask
     * @throws OutputWriteFailedException
     */
    private static ObjectMask maybePadObject(ObjectMask object, ImageDimensions dimensions, Padding padding) {

        if (padding.noPadding()) {
            return object;
        }

        BoundingBox bboxToExtract =
                object.getBoundingBox()
                        .growBy(
                                padding.asPoint(),
                                dimensions.getExtent());

        return object.mapBoundingBoxChangeExtent(bboxToExtract);
    }
    
    private static ObjectMask extractObjectKeepEntireImage(ObjectMask object, ImageDimensions dimensions) {
        return object.mapBoundingBoxChangeExtent(
                new BoundingBox(dimensions.getExtent()));
    }
}
