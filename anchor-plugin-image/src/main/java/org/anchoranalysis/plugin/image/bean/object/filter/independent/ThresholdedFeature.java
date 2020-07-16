/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterRelation;

/**
 * Only keeps objects whose feature-value satisfies a condition relative to a threshold.
 *
 * <p>Specifically, <code>relation(featureValue,threshold)</code> must be true.
 *
 * @author Owen Feehan
 */
public class ThresholdedFeature extends ObjectFilterRelation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;

    @BeanField @Getter @Setter private double threshold;

    @BeanField @Getter @Setter private boolean debug = true;
    // END BEAN PROPERTIES

    private FeatureCalculatorSingle<FeatureInputSingleObject> featureSession = null;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dim, objectsToFilter);

        // This could be called many times, so we create a new feature session only on the first
        // occasion
        if (featureSession == null) {
            featureSession = featureEvaluator.createAndStartSession();
        }

        if (debug) {
            getLogger().messageLogger().log("START Feature Threshold");
        }
    }

    @Override
    protected boolean match(
            ObjectMask object, Optional<ImageDimensions> dim, RelationToValue relation)
            throws OperationFailedException {

        double val;
        try {
            val = featureSession.calc(new FeatureInputSingleObject(object));
        } catch (FeatureCalcException e) {
            throw new OperationFailedException(e);
        }

        boolean succ = (relation.isRelationToValueTrue(val, threshold));

        if (debug) {
            if (succ) {
                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "%s\tVal=%f\tthreshold=%f\t  (Accepted)",
                                object.centerOfGravity(), val, threshold);
            } else {
                getLogger()
                        .messageLogger()
                        .logFormatted(
                                "%s\tVal=%f\tthreshold=%f",
                                object.centerOfGravity(), val, threshold);
            }
        }

        return succ;
    }

    @Override
    protected void end() throws OperationFailedException {
        super.end();
        if (debug) {
            getLogger().messageLogger().log("END Feature Threshold");
        }
    }
}
