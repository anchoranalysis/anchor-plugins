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

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.session.calculator.single.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/**
 * Finds the object with the maximum feature among a group of matches for each object.
 *
 * <p>Specifically, for each object in the upstream collection, matches are found, and the object
 * from those matches with the highest feature value is selected for inclusion in the newly created
 * collection.
 *
 * <p>If every object has at least one match, the newly created collection will have as many
 * elements as the upstream collection.
 *
 * @author Owen Feehan
 */
public class ObjectWithMaximumFeatureFromEachMatchedCollection
        extends ObjectCollectionProviderWithFeature {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectMatcher matcher;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws ProvisionFailedException {

        FeatureCalculatorSingle<FeatureInputSingleObject> session = createSession();

        try {
            List<MatchedObject> listMatches = matcher.findMatch(objects);

            return ObjectCollectionFactory.mapFromOptional(
                    listMatches, owm -> findMax(session, owm.getMatches()));

        } catch (OperationFailedException | FeatureCalculationException e) {
            throw new ProvisionFailedException(e);
        }
    }

    private Optional<ObjectMask> findMax(
            FeatureCalculatorSingle<FeatureInputSingleObject> session, ObjectCollection objects)
            throws FeatureCalculationException {
        Optional<ObjectMask> max = Optional.empty();
        double maxVal = 0;

        for (ObjectMask objectMask : objects) {

            double featureVal = session.calculate(new FeatureInputSingleObject(objectMask));

            if (!max.isPresent() || featureVal > maxVal) {
                max = Optional.of(objectMask);
                maxVal = featureVal;
            }
        }

        return max;
    }
}
