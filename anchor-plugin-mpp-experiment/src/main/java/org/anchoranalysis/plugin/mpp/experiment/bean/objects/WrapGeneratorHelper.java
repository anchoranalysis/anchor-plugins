package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class WrapGeneratorHelper {
    
    public static IterableGenerator<ObjectMask> boundingBoxAsObject(
            IterableGenerator<BoundingBox> generator, boolean mip) {
        return new IterableGeneratorBridge<>(
                generator, sourceObject -> boundingBoxFromObject(sourceObject, mip));
    }

    private static BoundingBox boundingBoxFromObject(ObjectMask object, boolean mip) {
        if (mip) {
            return object.getBoundingBox().flattenZ();
        } else {
            return object.getBoundingBox();
        }
    }
}
