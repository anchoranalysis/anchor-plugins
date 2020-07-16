/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.feature;

import java.util.SortedSet;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Creates a new object-collection with objects sorted by a derived feature-value (in ascending
 * order)
 *
 * <p>A new object-collection is always created, but the objects themselves are identical to the
 * upstream collection.
 *
 * @author Owen Feehan
 */
public class SortByFeature extends ObjectCollectionProviderWithFeature {

    /** Associates a feature-value with an object so it can be sorted by the feature-value */
    @AllArgsConstructor
    @EqualsAndHashCode
    private static class ObjectWithFeatureValue implements Comparable<ObjectWithFeatureValue> {

        @Getter private final ObjectMask object;
        private final double featureVal;

        @Override
        public int compareTo(ObjectWithFeatureValue other) {
            return Double.valueOf(other.featureVal).compareTo(featureVal);
        }
    }

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        try {
            FeatureCalculatorSingle<FeatureInputSingleObject> featureSession = createSession();

            SortedSet<ObjectWithFeatureValue> sorted =
                    objects.stream()
                            .mapToSortedSet(
                                    objectMask ->
                                            new ObjectWithFeatureValue(
                                                    objectMask,
                                                    featureSession.calc(
                                                            new FeatureInputSingleObject(
                                                                    objectMask))));

            return ObjectCollectionFactory.mapFrom(sorted, ObjectWithFeatureValue::getObject);

        } catch (FeatureCalcException e) {
            throw new CreateException(e);
        }
    }
}
