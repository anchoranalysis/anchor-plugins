/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

@RequiredArgsConstructor
public class IncreaseFeatureCondition implements AfterCondition {

    private final FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;

    private FeatureCalculatorSingle<FeatureInputSingleObject> session;

    @Override
    public void init(Logger logger) throws InitException {

        if (featureEvaluator != null) {
            try {
                session = featureEvaluator.createAndStartSession();
            } catch (OperationFailedException e) {
                throw new InitException(e);
            }
        } else {
            session = null;
        }
    }

    @Override
    public boolean accept(
            ObjectMask source,
            ObjectMask destination,
            ObjectMask merged,
            Optional<ImageResolution> res)
            throws OperationFailedException {

        if (session != null) {
            return doesIncreaseFeatureValueForBoth(source, destination, merged);
        } else {
            return true;
        }
    }

    private boolean doesIncreaseFeatureValueForBoth(
            ObjectMask source, ObjectMask destination, ObjectMask merged)
            throws OperationFailedException {

        // Feature source
        try {
            double featureSrc = calc(source);
            double featureDest = calc(destination);
            double featureMerge = calc(merged);

            // We skip if we don't increase the feature value for both objects
            return (featureMerge > featureSrc && featureMerge > featureDest);

        } catch (FeatureCalcException e) {
            throw new OperationFailedException(e);
        }
    }

    private double calc(ObjectMask object) throws FeatureCalcException {
        return session.calc(new FeatureInputSingleObject(object));
    }
}
