/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.image.bean.object.provider.Reference;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.PairNeighbors;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.source.FromObjects;
import org.anchoranalysis.test.TestLoader;

class TaskFixtureObjects
        extends TaskFixture<
                MultiInput,
                FeatureInputSingleObject,
                FeatureTableCalculator<FeatureInputSingleObject>> {

    private CombineObjectsForFeatures<?> flexiFeatureTable = new EachObjectIndependently();

    public TaskFixtureObjects(TestLoader loader) throws CreateException {
        super(loader);
    }

    /**
     * Change to use Merged-Pairs mode rather than Simple mode
     *
     * @param includeFeaturesInPair iff true "pair" features are populated in merged-pair mode
     */
    public void changeToMergedPairs(boolean includeFeaturesInPair, boolean includeImageFeatures) {
        flexiFeatureTable = createMergedPairs(includeFeaturesInPair, includeImageFeatures);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected FromObjects<FeatureInputSingleObject> createSource(
            EnergyStackWithoutParams energyStack, FeaturesLoader featureLoader)
            throws CreateException {
        FromObjects<FeatureInputSingleObject> source = new FromObjects<>();
        source.setDefine(DefineFixture.create(energyStack, Optional.of(featureLoader.shared())));
        source.setCombine((CombineObjectsForFeatures<FeatureInputSingleObject>) flexiFeatureTable);
        source.setObjects(createObjectProviders(MultiInputFixture.OBJECTS_NAME));
        return source;
    }

    @Override
    protected List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> createFeatures(
            FeaturesLoader featureLoader) {
        return featureLoader.single();
    }

    private static List<NamedBean<ObjectCollectionProvider>> createObjectProviders(
            String objectsName) {
        return Arrays.asList(new NamedBean<>(objectsName, new Reference(objectsName)));
    }

    private PairNeighbors createMergedPairs(
            boolean includeFeaturesInPair, boolean includeImageFeatures) {
        PairNeighbors mergedPairs = new PairNeighbors();
        if (includeFeaturesInPair) {
            mergedPairs.setFeaturesPair(featureLoader.pair());
        }
        if (includeImageFeatures) {
            mergedPairs.setFeaturesImage(featureLoader.image());
        }
        return mergedPairs;
    }
}
