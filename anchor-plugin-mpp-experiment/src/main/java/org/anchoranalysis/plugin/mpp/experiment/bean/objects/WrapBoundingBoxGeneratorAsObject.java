package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;

/**
 * Exposes an iterable generator that accepts bounding-boxes as one that accepts object-masks
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WrapBoundingBoxGeneratorAsObject {

    /**
     * Gives an existing generator that accepts bounding-boxes an interface that accepts
     * object-masks
     *
     * @param generator existing generator to wrap
     * @param flatten whether the bounding-box should be flattened in the z dimension
     * @return the wrapped generator
     */
    public static IterableGenerator<ObjectMask> wrap(
            IterableGenerator<BoundingBox> generator, boolean flatten) {
        return new IterableGeneratorBridge<>(
                generator, sourceObject -> boundingBoxFromObject(sourceObject, flatten));
    }

    private static BoundingBox boundingBoxFromObject(ObjectMask object, boolean flatten) {
        if (flatten) {
            return object.getBoundingBox().flattenZ();
        } else {
            return object.getBoundingBox();
        }
    }
}
