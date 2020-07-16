/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.feature;

import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Finds the object with the maximum feature-value from a collection.
 *
 * @author Owen Feehan
 */
public class ObjectWithMaximumFeature extends ObjectCollectionProviderWithFeature {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        Optional<ObjectMask> max = findMaxObj(createSession(), objects);

        if (max.isPresent()) {
            return ObjectCollectionFactory.from(max.get());
        } else {
            return ObjectCollectionFactory.empty();
        }
    }

    private Optional<ObjectMask> findMaxObj(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, ObjectCollection objects)
            throws CreateException {

        try {
            ObjectMask max = null;

            double maxVal = 0;
            for (ObjectMask object : objects) {

                double featureVal = session.calc(new FeatureInputSingleObject(object));

                if (max == null || featureVal > maxVal) {
                    max = object;
                    maxVal = featureVal;
                }
            }

            return Optional.ofNullable(max);

        } catch (FeatureCalcException e) {
            throw new CreateException(e);
        }
    }
}
