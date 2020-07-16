/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.pair.overlap;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.orientation.DirectionVector;

/**
 * TODO the center-of-gravity calculation can be turned into a FeatureCalculation which is cacheable
 *
 * @author Owen Feehan
 */
public class CostOverlapWithinMidpointDistance extends FeaturePairObjects {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private UnitValueDistance maxDistance;

    @BeanField @Getter @Setter private double minOverlap = 0.6;

    @BeanField @Getter @Setter private boolean suppressZ = true;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputPairObjects> input) throws FeatureCalcException {

        FeatureInputPairObjects inputSessionless = input.get();

        if (isDistanceMoreThanMax(inputSessionless)) {
            return 1.0;
        }

        double overlapRatio = OverlapRatioUtilities.overlapRatioToMaxVolume(inputSessionless);

        if (overlapRatio > minOverlap) {
            return 1.0 - overlapRatio;
        } else {
            return 1.0;
        }
    }

    private boolean isDistanceMoreThanMax(FeatureInputPairObjects params)
            throws FeatureCalcException {

        if (!params.getResOptional().isPresent()) {
            throw new FeatureCalcException("This feature requires an Image-Res in the input");
        }

        Point3d cog1 = params.getFirst().centerOfGravity();
        Point3d cog2 = params.getSecond().centerOfGravity();

        double distance = calcDistance(cog1, cog2);
        try {
            return distance > calcMaxDistance(cog1, cog2, params.getResOptional());
        } catch (OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    private double calcDistance(Point3d cog1, Point3d cog2) {
        if (suppressZ) {
            cog1.setZ(0);
            cog2.setZ(0);
        }
        return cog1.distance(cog2);
    }

    // We measure the euclidian distance between center-points
    private double calcMaxDistance(Point3d cog1, Point3d cog2, Optional<ImageResolution> res)
            throws OperationFailedException {
        DirectionVector vec = DirectionVector.createBetweenTwoPoints(cog1, cog2);
        return maxDistance.resolve(res, vec);
    }
}
