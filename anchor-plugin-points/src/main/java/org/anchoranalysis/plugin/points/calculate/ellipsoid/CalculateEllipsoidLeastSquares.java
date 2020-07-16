/* (C)2020 */
package org.anchoranalysis.plugin.points.calculate.ellipsoid;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.points.calculate.CalculatePointsFromOutline;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public class CalculateEllipsoidLeastSquares
        extends FeatureCalculation<MarkEllipsoid, FeatureInputSingleObject> {

    private final boolean suppressZCovariance;
    private final ResolvedCalculation<List<Point3i>, FeatureInputSingleObject> ccPoints;

    public static MarkEllipsoid of(
            SessionInput<FeatureInputSingleObject> input, boolean suppressZCovariance)
            throws FeatureCalcException {

        ResolvedCalculation<List<Point3i>, FeatureInputSingleObject> ccPoints =
                input.resolver().search(new CalculatePointsFromOutline());

        ResolvedCalculation<MarkEllipsoid, FeatureInputSingleObject> ccEllipsoid =
                input.resolver()
                        .search(new CalculateEllipsoidLeastSquares(suppressZCovariance, ccPoints));
        return input.calc(ccEllipsoid);
    }

    @Override
    protected MarkEllipsoid execute(FeatureInputSingleObject input) throws FeatureCalcException {

        try {
            // Shell Rad is arbitrary here for now
            return EllipsoidFactory.createMarkEllipsoidLeastSquares(
                    new CachedCalculationOperation<>(ccPoints, input),
                    input.getDimensionsRequired(),
                    suppressZCovariance,
                    0.2);
        } catch (CreateException e) {
            throw new FeatureCalcException(e);
        }
    }
}
