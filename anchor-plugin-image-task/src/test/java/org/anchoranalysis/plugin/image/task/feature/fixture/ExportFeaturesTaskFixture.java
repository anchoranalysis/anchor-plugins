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

package org.anchoranalysis.plugin.image.task.feature.fixture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.io.imagej.bean.interpolator.ImageJ;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.bean.grouper.Grouper;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.EnergyStackFixture;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.mockito.Mockito;

/**
 * A fixture that creates a {@link ExportFeatures} task.
 *
 * @param <S> input-type from which one or more rows of features are derived
 * @param <T> feature-input type for {@code features} bean-field
 * @param <V> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 */
@Accessors(fluent = true)
public abstract class ExportFeaturesTaskFixture<
        S extends InputFromManager, T extends FeatureInputEnergy, V> {

    /** The group name used for grouping. */
    private static final String GROUP = "arbitraryGroup";

    /** The energy stack used for feature calculations. */
    @Getter private EnergyStackWithoutParameters energyStack;

    /** The loader for feature-related objects. */
    @Getter protected final FeaturesLoader featureLoader;

    /**
     * Create for a particular {@link TestLoader}.
     *
     * <p>By default, use a big-sized energy-stack that functions with our feature-lists.
     *
     * @param loader the loader.
     * @throws CreateException if an energy-stack cannot be created.
     */
    public ExportFeaturesTaskFixture(TestLoader loader) throws CreateException {
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

    /**
     * Creates an {@link ExportFeatures} task.
     *
     * @return the created task
     * @throws CreateException if the task cannot be created
     */
    public ExportFeatures<S, V, T> createTask() throws CreateException {

        ExportFeatures<S, V, T> task = new ExportFeatures<>();
        task.setSource(createSource(energyStack, featureLoader));
        task.setFeatures(createFeatures(featureLoader));
        task.setFeaturesAggregate(featureLoader.aggregated());

        task.setGroup(createGrouperMock());

        BeanInstanceMapFixture.ensureInterpolator(new ImageJ());
        BeanInstanceMapFixture.ensureStackDisplayer();
        BeanInstanceMapFixture.check(task);

        return task;
    }

    /**
     * Creates a {@link FeatureSource} for the task.
     *
     * @param energyStack the energy stack to use
     * @param featureLoader the feature loader to use
     * @return the created feature source
     * @throws CreateException if the source cannot be created
     */
    protected abstract FeatureSource<S, V, T> createSource(
            EnergyStackWithoutParameters energyStack, FeaturesLoader featureLoader)
            throws CreateException;

    /**
     * Creates a list of {@link FeatureListProvider}s for the task.
     *
     * @param featureLoader the feature loader to use
     * @return the created list of feature list providers
     */
    protected abstract List<NamedBean<FeatureListProvider<T>>> createFeatures(
            FeaturesLoader featureLoader);

    /**
     * Creates an {@link EnergyStackWithoutParameters} with specified properties.
     *
     * @param bigSizeEnergy whether to create a big-sized energy stack
     * @param singleChannel whether to create a single-channel energy stack
     * @param includeResolution whether to include resolution information
     * @return the created energy stack
     */
    private EnergyStackWithoutParameters createEnergyStack(
            boolean bigSizeEnergy, boolean singleChannel, boolean includeResolution) {
        return EnergyStackFixture.create(bigSizeEnergy, false, singleChannel, includeResolution)
                .withoutParameters();
    }

    /**
     * Creates a mock {@link Grouper} that always assigns a constant string as the group.
     *
     * @return the created mock grouper
     */
    private static Grouper createGrouperMock() {
        Grouper grouper = Mockito.mock(Grouper.class);
        when(grouper.createInputGrouper(any())).thenReturn(Optional.of(value -> GROUP));
        return grouper;
    }
}
