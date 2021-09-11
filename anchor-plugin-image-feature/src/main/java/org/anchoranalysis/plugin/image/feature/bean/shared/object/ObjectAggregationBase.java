/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import cern.colt.list.DoubleArrayList;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.CalculateForChild;
import org.anchoranalysis.feature.calculate.cache.ChildCacheName;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;

/**
 * Calculates a feature for a set of objects using this stack as a energy-stack, and aggregates.
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
public abstract class ObjectAggregationBase<T extends FeatureInputEnergy>
        extends FeatureSingleObjectFromShared<T> {

    // START BEAN PROPERTIES
    @BeanField @SkipInit @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    // We cache the objectCollection as it's not dependent on individual parameters
    private ObjectCollection createdObjects;

    @Override
    protected void beforeCalcWithInitialization(ImageInitialization params) throws InitializeException {
        objects.initializeRecursive(params, getLogger());
    }

    @Override
    protected double calc(
            CalculateForChild<T> calculateForChild,
            Feature<FeatureInputSingleObject> featureForSingleObject)
            throws FeatureCalculationException {

        if (createdObjects == null) {
            createdObjects = createObjects();
        }

        return deriveStatistic(
                featureValsForObjects(featureForSingleObject, calculateForChild, createdObjects));
    }

    protected abstract double deriveStatistic(DoubleArrayList featureVals);

    private ObjectCollection createObjects() throws FeatureCalculationException {
        try {
            return objects.get();
        } catch (ProvisionFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private DoubleArrayList featureValsForObjects(
            Feature<FeatureInputSingleObject> feature,
            CalculateForChild<T> calculateForChild,
            ObjectCollection objects)
            throws FeatureCalculationException {
        DoubleArrayList featureVals = new DoubleArrayList();

        // Calculate a feature on each object-mask
        for (int i = 0; i < objects.size(); i++) {

            double value =
                    calculateForChild.calculate(
                            feature, new CalculateInputFromStack<>(objects, i), cacheName(i));
            featureVals.add(value);
        }
        return featureVals;
    }

    private ChildCacheName cacheName(int index) {
        return new ChildCacheName(
                ObjectAggregationBase.class, index + "_" + createdObjects.hashCode());
    }
}
