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
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.io.bean.path.derive.Constant;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.EnergyStackFixture;

/**
 * A fixture that creates a {@link ExportFeatures} task.
 *
 * @param <S> input-type from which one or more rows of features are derived
 * @param <T> feature-input type for {@code features} bean-field
 * @param <V> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 */
@Accessors(fluent = true)
abstract class TaskFixture<S extends InputFromManager, T extends FeatureInputEnergy, V> {

    @Getter private EnergyStackWithoutParameters energyStack;

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
        this.energyStack = createEnergyStack(true, false, true);
        this.featureLoader = new FeaturesLoader(loader);
    }

    /** Change to using a small energy-stack that causes some features to throw errors. */
    public void useSmallEnergy() {
        this.energyStack = createEnergyStack(false, false, true);
    }

    /** Change to a single channel energy-stack. */
    public void useSingleChannelEnergy() {
        this.energyStack = createEnergyStack(true, true, true);
    }

    /** Change to a three channel big energy-stack, without resolution. */
    public void removeResolution() {
        this.energyStack = createEnergyStack(true, false, false);
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
            EnergyStackWithoutParameters energyStack, FeaturesLoader featureLoader)
            throws CreateException;

    protected abstract List<NamedBean<FeatureListProvider<T>>> createFeatures(
            FeaturesLoader featureLoader);

    private EnergyStackWithoutParameters createEnergyStack(
            boolean bigSizeEnergy, boolean singleChannel, boolean includeResolution) {
        return EnergyStackFixture.create(bigSizeEnergy, false, singleChannel, includeResolution)
                .withoutParameters();
    }
}
