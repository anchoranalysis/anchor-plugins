/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.touching;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.object.ObjectMask;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateIntersectionOfDilatedBoundingBox
        extends FeatureCalculation<Optional<BoundingBox>, FeatureInputPairObjects> {

    private final boolean do3D;

    @Override
    protected Optional<BoundingBox> execute(FeatureInputPairObjects input)
            throws FeatureCalcException {
        return findIntersectionOfDilatedBoundingBox(
                input.getFirst(), input.getSecond(), input.getDimensionsRequired().getExtent());
    }

    private Optional<BoundingBox> findIntersectionOfDilatedBoundingBox(
            ObjectMask first, ObjectMask second, Extent extent) {

        // Grow each bounding box
        BoundingBox bboxFirst = dilatedBoundingBoxFor(first, extent);
        BoundingBox bboxSecond = dilatedBoundingBoxFor(second, extent);

        // Find the intersection
        return bboxFirst.intersection().withInside(bboxSecond, extent);
    }

    private BoundingBox dilatedBoundingBoxFor(ObjectMask object, Extent extent) {
        return object.getVoxelBoxBounded().dilate(do3D, Optional.of(extent));
    }
}
