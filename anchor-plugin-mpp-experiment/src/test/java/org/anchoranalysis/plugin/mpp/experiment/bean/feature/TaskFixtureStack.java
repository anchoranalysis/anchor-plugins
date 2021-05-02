package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FromImage;
import org.anchoranalysis.test.TestLoader;

class TaskFixtureStack
        extends TaskFixture<ProvidesStackInput, FeatureInputStack, FeatureList<FeatureInputStack>> {

    public TaskFixtureStack(TestLoader loader) throws CreateException {
        super(loader);
    }

    @Override
    protected FeatureSource<ProvidesStackInput, FeatureList<FeatureInputStack>, FeatureInputStack>
            createSource(EnergyStackWithoutParams energyStack, FeatureListLoader featureLoader)
                    throws CreateException {
        return new FromImage();
    }

    @Override
    protected List<NamedBean<FeatureListProvider<FeatureInputStack>>> createFeatures(
            FeatureListLoader featureLoader) {
        return featureLoader.image();
    }
}
