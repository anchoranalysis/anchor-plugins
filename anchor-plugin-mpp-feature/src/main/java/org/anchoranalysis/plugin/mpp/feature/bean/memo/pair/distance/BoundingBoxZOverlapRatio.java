/* (C)2020 */
package org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.distance;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.FeaturePairMemoSingleRegion;

/**
 * Measures the amount of bounding box overlap in the Z dimension
 *
 * <p>Expresses it as a fraction of the minimum Z-extent
 *
 * <p>This is useful for measuring how much two objects overlap in Z
 *
 * <p>It is only calculated if there is overlap of the bounding boxes in XYZ, else 0 is returned
 */
public class BoundingBoxZOverlapRatio extends FeaturePairMemoSingleRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean normalize = true;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairMemo> input) throws FeatureCalcException {

        FeatureInputPairMemo inputSessionless = input.get();

        BoundingBox bbox1 = bbox(inputSessionless, FeatureInputPairMemo::getObj1);
        BoundingBox bbox2 = bbox(inputSessionless, FeatureInputPairMemo::getObj2);

        // Check the bounding boxes intersect in general (including XY)
        if (!bbox1.intersection().existsWith(bbox2)) {
            return 0.0;
        }

        return calcOverlap(bbox1, bbox2, inputSessionless.getDimensionsRequired());
    }

    private double calcOverlap(BoundingBox bbox1, BoundingBox bbox2, ImageDimensions dim) {

        Optional<BoundingBox> bboxOverlap = bbox1.intersection().withInside(bbox2, dim.getExtent());
        if (!bboxOverlap.isPresent()) {
            return 0;
        }

        int minExtentZ = Math.min(zFor(bbox1), zFor(bbox2));

        double overlapZ = (double) zFor(bboxOverlap.get());

        if (normalize) {
            return overlapZ / minExtentZ;
        } else {
            return overlapZ;
        }
    }

    private static int zFor(BoundingBox bbox) {
        return bbox.extent().getZ();
    }
}
