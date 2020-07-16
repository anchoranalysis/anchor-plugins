/* (C)2020 */
package org.anchoranalysis.plugin.points.calculate.ellipse;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.LinearLeastSquaresEllipseFitter;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@EqualsAndHashCode(callSuper = false)
public class CalculateEllipseLeastSquares
        extends FeatureCalculation<ObjectWithEllipse, FeatureInputSingleObject> {

    @EqualsAndHashCode.Exclude private EllipseFactory factory;

    public CalculateEllipseLeastSquares() {
        factory = new EllipseFactory(new LinearLeastSquaresEllipseFitter());
    }

    @Override
    protected ObjectWithEllipse execute(FeatureInputSingleObject input)
            throws FeatureCalcException {

        try {
            NRGStack nrgStack = input.getNrgStackRequired().getNrgStack();

            ObjectMask object = extractEllipseSlice(input.getObject());

            // Shell Rad is arbitrary here for now
            MarkEllipse mark = factory.create(object, nrgStack.getDimensions(), 0.2);

            return new ObjectWithEllipse(object, mark);
        } catch (CreateException | InsufficientPointsException e) {
            throw new FeatureCalcException(e);
        }
    }

    private static ObjectMask extractEllipseSlice(ObjectMask object) {
        int zSliceCenter = (int) object.centerOfGravity().getZ();
        return object.extractSlice(
                zSliceCenter - object.getBoundingBox().cornerMin().getZ(), false);
    }
}
