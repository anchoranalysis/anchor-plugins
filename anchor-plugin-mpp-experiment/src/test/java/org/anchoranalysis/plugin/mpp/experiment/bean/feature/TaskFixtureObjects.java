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
            EnergyStackWithoutParams energyStack, FeatureListLoader featureLoader)
            throws CreateException {
        FromObjects<FeatureInputSingleObject> source = new FromObjects<>();
        source.setDefine(DefineFixture.create(energyStack, Optional.of(featureLoader.shared())));
        source.setCombine((CombineObjectsForFeatures<FeatureInputSingleObject>) flexiFeatureTable);
        source.setObjects(createObjectProviders(MultiInputFixture.OBJECTS_NAME));
        return source;
    }

    @Override
    protected List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> createFeatures(
            FeatureListLoader featureLoader) {
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
