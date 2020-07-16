/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodMask;

/**
 * Base class for features that calculate touching with a dilated bounding box intersection
 *
 * @author Owen Feehan
 */
public abstract class TouchingVoxels extends FeaturePairObjects {

    // START BEAN PROPERTIES
    @BeanField private boolean do3D = true;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {

        FeatureInputPairObjects inputSessionless = input.get();

        Optional<BoundingBox> bboxIntersect = bboxIntersectDilated(input);

        if (!bboxIntersect.isPresent()) {
            // No intersection, so therefore return 0
            return 0;
        }

        return calcWithIntersection(
                inputSessionless.getFirst(), inputSessionless.getSecond(), bboxIntersect.get());
    }

    protected abstract double calcWithIntersection(
            ObjectMask object1, ObjectMask object2, BoundingBox bboxIntersect)
            throws FeatureCalcException;

    /**
     * The intersection of the bounding box of one mask with the (dilated by 1 bounding-box) of the
     * other
     */
    private Optional<BoundingBox> bboxIntersectDilated(SessionInput<FeatureInputPairObjects> input)
            throws FeatureCalcException {
        return input.calc(new CalculateIntersectionOfDilatedBoundingBox(do3D));
    }

    protected CountKernel createCountKernelMask(ObjectMask object1, ObjectMask object2Relative) {
        return new CountKernelNeighborhoodMask(
                do3D, object1.getBinaryValuesByte(), object2Relative, true);
    }

    public boolean isDo3D() {
        return do3D;
    }

    public void setDo3D(boolean do3d) {
        do3D = do3d;
    }
}
