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
package org.anchoranalysis.plugin.image.task.feature;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FeatureSource;
import org.anchoranalysis.plugin.image.task.bean.feature.source.FromImage;
import org.anchoranalysis.plugin.image.task.feature.fixture.ExportFeaturesTaskFixture;
import org.anchoranalysis.plugin.image.task.feature.fixture.FeaturesLoader;
import org.anchoranalysis.test.TestLoader;

class TaskFixtureStack
        extends ExportFeaturesTaskFixture<
                ProvidesStackInput, FeatureInputStack, FeatureList<FeatureInputStack>> {

    public TaskFixtureStack(TestLoader loader) throws CreateException {
        super(loader);
    }

    @Override
    protected FeatureSource<ProvidesStackInput, FeatureList<FeatureInputStack>, FeatureInputStack>
            createSource(EnergyStackWithoutParameters energyStack, FeaturesLoader featureLoader)
                    throws CreateException {
        return new FromImage();
    }

    @Override
    protected List<NamedBean<FeatureListProvider<FeatureInputStack>>> createFeatures(
            FeaturesLoader featureLoader) {
        return featureLoader.imageExpanded();
    }
}
