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
package org.anchoranalysis.plugin.mpp.experiment.bean.feature.image;

import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.ExportFeaturesTestBase;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.task.ExportFeaturesTaskFixture;
import org.junit.jupiter.api.Test;

class ExportFeaturesImageTest
        extends ExportFeaturesTestBase<
                ProvidesStackInput,
                FeatureInputStack,
                FeatureList<FeatureInputStack>,
                TaskFixtureStack> {

    private static final String EXPECTED_OUTPUT_SUBDIRECTORY = "stack";

    public static final String OUTPUT_DIRECTORY_SINGLE_CHANNEL = "singleChannel/";

    public static final String OUTPUT_DIRECTORY_THREE_CHANNELS = "threeChannels/";

    public static final String OUTPUT_DIRECTORY_WITHOUT_RESOLUTION = "withoutResolution/";

    ExportFeaturesImageTest() {
        super(EXPECTED_OUTPUT_SUBDIRECTORY, false, TaskFixtureStack::new);
    }

    /** Tests a single channel image <b>with</b> an accompanying image resolution. */
    @Test
    void testSingle() throws OperationFailedException {
        testOnTask(
                OUTPUT_DIRECTORY_SINGLE_CHANNEL, ExportFeaturesTaskFixture::useSingleChannelEnergy);
    }

    /** Tests a three channel image <b>with</b> an accompanying image resolution. */
    @Test
    void testThree() throws OperationFailedException {
        testOnTask(
                OUTPUT_DIRECTORY_THREE_CHANNELS, fixture -> {} // Change nothing
                );
    }

    /** Tests a single channel image <b>without</b> an accompanying image resolution. */
    @Test
    void testWithoutResolution() throws OperationFailedException {
        testOnTask(
                OUTPUT_DIRECTORY_WITHOUT_RESOLUTION, ExportFeaturesTaskFixture::removeResolution);
    }
}
