/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.border;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;

public class NumberVoxelsAtBorder extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outsideAtThreshold = false;

    @BeanField @Getter @Setter private boolean ignoreAtThreshold = false;

    @BeanField @Getter @Setter private boolean do3D = false;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
        return (double)
                numBorderPixels(
                        input.get().getObject(), ignoreAtThreshold, outsideAtThreshold, do3D);
    }

    public static int numBorderPixels(
            ObjectMask object,
            boolean ignoreAtThreshold,
            boolean outsideAtThreshold,
            boolean do3D) {
        OutlineKernel3 kernel =
                new OutlineKernel3(
                        object.getBinaryValuesByte(), outsideAtThreshold, do3D, ignoreAtThreshold);
        return ApplyKernel.applyForCount(kernel, object.getVoxelBox());
    }
}
