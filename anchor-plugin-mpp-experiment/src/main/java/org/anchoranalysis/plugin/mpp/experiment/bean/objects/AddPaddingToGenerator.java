package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.size.Padding;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ObjectsWithBoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Adds optional padding to objects before being passed into another generator
 * <p>
 * TODO This is quite inefficient as it changes the object-mask's voxel-buffers to use the ENTIRE
 * image each time. There's a better way to do this.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class AddPaddingToGenerator {

    public static IterableGenerator<ObjectsWithBoundingBox> addPadding(
            IterableGenerator<ObjectsWithBoundingBox> generator,
            ImageDimensions dimensions,
            Padding padding,
            boolean keepEntireImage) {
        // Maybe we need to change the objectMask to a padded version
        return IterableGeneratorBridge.createOneToOne(
                generator,
                objects -> {
                    if (keepEntireImage) {
                        return objects.mapObjectsToUseEntireImage(dimensions);
                    } else {
                        return maybePadObjects(objects, dimensions, padding);
                    }
                });
    }
    
    private static ObjectsWithBoundingBox maybePadObjects(ObjectsWithBoundingBox objects, ImageDimensions dimensions, Padding padding) throws OperationFailedException {
       if (objects.numberObjects()==1) {
           return new ObjectsWithBoundingBox( maybePadObject(objects.get(0), dimensions, padding) );
       } else {
           throw new OperationFailedException("Padding is only supported for single-objects");
       }
    }
    

    /**
     * Adds padding (if set) to an object-mask
     *
     * @param object object-mask to be padded
     * @param dimensions size of image
     * @return either the exist object-mask (if no padding is to be added) or a padded object-mask
     * @throws OutputWriteFailedException
     */
    private static ObjectMask maybePadObject(
            ObjectMask object, ImageDimensions dimensions, Padding padding) {

        if (padding.noPadding()) {
            return object;
        }

        BoundingBox bboxToExtract =
                object.getBoundingBox().growBy(padding.asPoint(), dimensions.getExtent());

        return object.mapBoundingBoxChangeExtent(bboxToExtract);
    }
}
