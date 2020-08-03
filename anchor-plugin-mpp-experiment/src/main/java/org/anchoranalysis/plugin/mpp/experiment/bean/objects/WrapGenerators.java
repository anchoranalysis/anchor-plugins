package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.stream.Stream;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.io.generator.raster.bbox.ObjectsWithBoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;

/**
 * Exposes an iterable generator that accepts other kinds of objects as one that accepts a {@link ObjectsWithBoundingBox}
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WrapGenerators {

    /**
     * Gives an existing generator that accepts bounding-boxes an interface that accepts
     * {@link ObjectsWithBoundingBox}
     *
     * @param generator existing generator to wrap
     * @param flatten whether the bounding-box should be flattened in the z dimension
     * @return the wrapped generator
     */
    public static IterableGenerator<ObjectsWithBoundingBox> wrapBoundingBox(
            IterableGenerator<BoundingBox> generator, boolean flatten) {
        return IterableGeneratorBridge.createOneToMany(
                generator, objects -> Stream.of(boundingBoxFromObject(objects, flatten)) );
    }

    /**
     * Gives an existing generator that accepts single-object masks an interface that accepts
     * {@link ObjectsWithBoundingBox}
     *
     * @param generator existing generator to wrap
     * @param flatten whether the bounding-box should be flattened in the z dimension
     * @return the wrapped generator
     */
    public static IterableGenerator<ObjectsWithBoundingBox> wrapObjectMask(
            IterableGenerator<ObjectMask> generator) {
        return IterableGeneratorBridge.createOneToMany(
                generator, objects -> objects.getObjects().streamStandardJava() );
    }
    
    private static BoundingBox boundingBoxFromObject(ObjectsWithBoundingBox objects, boolean flatten) {
        if (flatten) {
            return objects.getBoundingBox().flattenZ();
        } else {
            return objects.getBoundingBox();
        }
    }
}
