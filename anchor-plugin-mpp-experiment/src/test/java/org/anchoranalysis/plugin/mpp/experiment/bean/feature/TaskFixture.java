/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.io.bean.path.derive.Constant;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.EnergyStackFixture;

@Accessors(fluent = true)
abstract class TaskFixture<S extends InputFromManager, T extends FeatureInputEnergy, V> {

    @Getter private EnergyStackWithoutParams energyStack;

    @Getter protected final FeaturesLoader featureLoader;

    /**
     * Constructor
     *
     * <p>By default, use a big-sized energy-stack that functions with our feature-lists
     *
     * <p>By default, load the features from PATH_FEATURES
     *
     * <p>By default, use Simple feature-mode. It can be changed to Merged-Pairs.
     *
     * @param loader
     * @throws CreateException
     */
    public TaskFixture(TestLoader loader) throws CreateException {
        this.energyStack = createEnergyStack(true);
        this.featureLoader = new FeaturesLoader(loader);
    }

    /** Change to using a small energy-stack that causes some features to throw errors */
    public void useSmallEnergyInstead() {
        this.energyStack = createEnergyStack(false);
    }

    public ExportFeatures<S, V, T> createTask() throws CreateException {

        ExportFeatures<S, V, T> task = new ExportFeatures<>();
        task.setSource(createSource(energyStack, featureLoader));
        task.setFeatures(createFeatures(featureLoader));
        task.setFeaturesAggregate(featureLoader.aggregated());
        task.setGroup(new Constant("arbitraryGroup"));

        try {
            task.checkMisconfigured(RegisterBeanFactories.getDefaultInstances());
        } catch (BeanMisconfiguredException e) {
            throw new CreateException(e);
        }

        return task;
    }

    protected abstract FeatureSource<S, V, T> createSource(
            EnergyStackWithoutParams energyStack, FeaturesLoader featureLoader)
            throws CreateException;

    protected abstract List<NamedBean<FeatureListProvider<T>>> createFeatures(
            FeaturesLoader featureLoader);

    private EnergyStackWithoutParams createEnergyStack(boolean bigSizeEnergy) {
        return EnergyStackFixture.create(bigSizeEnergy, false).withoutParams();
    }
}
