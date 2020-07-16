/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.feature;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public abstract class ObjectCollectionProviderWithFeature extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;
    // END BEAN PROPERTIES

    protected FeatureCalculatorSingle<FeatureInputSingleObject> createSession()
            throws CreateException {
        try {
            return featureEvaluator.createAndStartSession();
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
