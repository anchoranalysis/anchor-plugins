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
 * Calculates a feature for a set of objects using this stack as an energy-stack, and aggregates.
 *
 * <p>This abstract class extends {@link FeatureSingleObjectFromShared} to provide functionality for
 * calculating features on a collection of objects and aggregating the results.
 *
 * @author Owen Feehan
 * @param <T> feature-input type that extends {@link FeatureInputEnergy}
 */
public abstract class ObjectAggregationBase<T extends FeatureInputEnergy>
        extends FeatureSingleObjectFromShared<T> {

    // START BEAN PROPERTIES
    /** The provider for the collection of objects to calculate features on. */
    @BeanField @SkipInit @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    /** The created object collection after initialization. */
    private ObjectCollection createdObjects;

    @Override
    protected void beforeCalcWithInitialization(ImageInitialization initialization)
            throws InitializeException {
        objects.initializeRecursive(initialization, getLogger());
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

    /**
     * Derives a statistic from the calculated feature values for all objects.
     *
     * @param featureVals the list of feature values for all objects
     * @return the derived statistic
     */
    protected abstract double deriveStatistic(DoubleArrayList featureVals);

    /**
     * Creates the object collection.
     *
     * @return the created {@link ObjectCollection}
     * @throws FeatureCalculationException if the object collection creation fails
     */
    private ObjectCollection createObjects() throws FeatureCalculationException {
        try {
            return objects.get();
        } catch (ProvisionFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    /**
     * Calculates feature values for all objects in the collection.
     *
     * @param feature the feature to calculate for each object
     * @param calculateForChild the calculation context for child features
     * @param objects the collection of objects to calculate features on
     * @return a list of feature values for all objects
     * @throws FeatureCalculationException if the feature calculation fails
     */
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

    /**
     * Creates a cache name for a specific object index.
     *
     * @param index the index of the object
     * @return the {@link ChildCacheName} for the object
     */
    private ChildCacheName cacheName(int index) {
        return new ChildCacheName(
                ObjectAggregationBase.class, index + "_" + createdObjects.hashCode());
    }
}
