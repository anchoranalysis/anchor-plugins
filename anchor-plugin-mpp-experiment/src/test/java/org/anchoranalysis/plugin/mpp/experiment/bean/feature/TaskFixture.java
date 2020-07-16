/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorConstant;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.image.bean.object.provider.Reference;
import org.anchoranalysis.plugin.image.feature.bean.object.table.FeatureTableObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.table.MergedPairs;
import org.anchoranalysis.plugin.image.feature.bean.object.table.Simple;
import org.anchoranalysis.plugin.image.task.bean.ExportFeaturesTask;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.source.FromObjects;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.NRGStackFixture;

@Accessors(fluent = true)
class TaskFixture {

    @Getter private NRGStack nrgStack;

    @Getter private final ExportObjectsFeatureLoader featureLoader;

    private FeatureTableObjects<?> flexiFeatureTable = new Simple();

    /**
     * Constructor
     *
     * <p>By default, use a big-sized NRG-stack that functions with our feature-lists
     *
     * <p>By default, load the features from PATH_FEATURES
     *
     * <p>By default, use Simple feature-mode. It can be changed to Merged-Pairs.
     *
     * @param loader
     * @throws CreateException
     */
    public TaskFixture(TestLoader loader) throws CreateException {
        this.nrgStack = createNRGStack(true);
        this.featureLoader = new ExportObjectsFeatureLoader(loader);
    }

    /** Change to using a small nrg-stack that causes some features to throw errors */
    public void useSmallNRGInstead() {
        this.nrgStack = createNRGStack(false);
    }

    /**
     * Change to use Merged-Pairs mode rather than Simple mode
     *
     * @param includeFeaturesInPair iff TRUE "pair" features are populated in merged-pair mode
     */
    public void changeToMergedPairs(boolean includeFeaturesInPair, boolean includeImageFeatures) {
        flexiFeatureTable = createMergedPairs(includeFeaturesInPair, includeImageFeatures);
    }

    public <T extends FeatureInput>
            ExportFeaturesTask<MultiInput, FeatureTableCalculator<T>, FeatureInputSingleObject>
                    createTask() throws CreateException {

        ExportFeaturesTask<MultiInput, FeatureTableCalculator<T>, FeatureInputSingleObject> task =
                new ExportFeaturesTask<>();
        task.setSource(createSource());
        task.setFeatures(featureLoader.single());
        task.setFeaturesAggregate(featureLoader.aggregated());
        task.setGroup(new FilePathGeneratorConstant("arbitraryGroup"));

        try {
            task.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());
        } catch (BeanMisconfiguredException e) {
            throw new CreateException(e);
        }

        return task;
    }

    @SuppressWarnings("unchecked")
    private <T extends FeatureInput> FromObjects<T> createSource() throws CreateException {
        FromObjects<T> source = new FromObjects<>();
        source.setDefine(DefineFixture.create(nrgStack, Optional.of(featureLoader.shared())));
        source.setTable((FeatureTableObjects<T>) flexiFeatureTable);
        source.setObjects(createObjectProviders(MultiInputFixture.OBJECTS_NAME));
        return source;
    }

    private static List<NamedBean<ObjectCollectionProvider>> createObjectProviders(
            String objectsName) {
        return Arrays.asList(new NamedBean<>(objectsName, new Reference(objectsName)));
    }

    private MergedPairs createMergedPairs(
            boolean includeFeaturesInPair, boolean includeImageFeatures) {
        MergedPairs mergedPairs = new MergedPairs();
        if (includeFeaturesInPair) {
            mergedPairs.setFeaturesPair(featureLoader.pair());
        }
        if (includeImageFeatures) {
            mergedPairs.setFeaturesImage(featureLoader.image());
        }
        return mergedPairs;
    }

    private NRGStack createNRGStack(boolean bigSizeNrg) {
        return NRGStackFixture.create(bigSizeNrg, false).getNrgStack();
    }
}
