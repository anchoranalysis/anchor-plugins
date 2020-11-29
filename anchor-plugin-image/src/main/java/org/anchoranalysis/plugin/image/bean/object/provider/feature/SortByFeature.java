/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.image.bean.object.provider.feature;

import java.util.SortedSet;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.session.calculator.single.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;

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
                                                    featureSession.calculate(
                                                            new FeatureInputSingleObject(
                                                                    objectMask))));

            return ObjectCollectionFactory.mapFrom(sorted, ObjectWithFeatureValue::getObject);

        } catch (FeatureCalculationException e) {
            throw new CreateException(e);
        }
    }
}
