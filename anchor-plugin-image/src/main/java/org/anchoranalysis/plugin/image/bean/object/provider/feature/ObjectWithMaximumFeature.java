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

import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.session.calculator.single.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;

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
            return ObjectCollectionFactory.of(max.get());
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

                double featureVal = session.calculate(new FeatureInputSingleObject(object));

                if (max == null || featureVal > maxVal) {
                    max = object;
                    maxVal = featureVal;
                }
            }

            return Optional.ofNullable(max);

        } catch (FeatureCalculationException e) {
            throw new CreateException(e);
        }
    }
}
