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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesStyle;
import org.anchoranalysis.plugin.image.task.feature.fixture.ExportFeaturesTaskFixture;
import org.anchoranalysis.plugin.image.task.feature.fixture.MultiInputFixture;
import org.anchoranalysis.plugin.image.task.feature.fixture.StackAsProviderFixture;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.experiment.task.ExecuteTaskHelper;
import org.anchoranalysis.test.image.io.BeanInstanceMapFixture;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportFeaturesImageTest {

    private static final String EXPECTED_OUTPUT_SUBDIRECTORY = "stack";

    public static final String OUTPUT_DIRECTORY_SINGLE_CHANNEL = "singleChannel/";

    public static final String OUTPUT_DIRECTORY_THREE_CHANNELS = "threeChannels/";

    public static final String OUTPUT_DIRECTORY_WITHOUT_RESOLUTION = "withoutResolution/";

    private static final Path EXPECTED_RESULTS_BASE = Paths.get("expectedOutputFeature");

    private static final String RELATIVE_PATH_SAVED_RESULTS =
            EXPECTED_RESULTS_BASE.resolve(EXPECTED_OUTPUT_SUBDIRECTORY).toString() + "/";

    private static TestLoader loader;

    private TaskFixtureStack taskFixture;

    static {
        BeanInstanceMapFixture.ensureStackWriter(true);
        BeanInstanceMapFixture.ensureStackDisplayer();
    }

    @TempDir Path directory;

    @BeforeAll
    static void setup() {

        RegisterBeanFactories.registerAllPackageBeanFactories();
        loader = TestLoader.createFromMavenWorkingDirectory();
    }

    @BeforeEach
    void setupTest() throws CreateException {
        taskFixture = new TaskFixtureStack(loader);
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

    /** Runs a test to check if the results of exporting-features correspond to saved-values. */
    private void testOnTask(String outputDirectory, Consumer<TaskFixtureStack> changeFixture)
            throws OperationFailedException {
        changeFixture.accept(taskFixture);
        testOnTask(outputDirectory);
    }

    /**
     * Runs a test to check if the results of exporting-features correspond to saved-values.
     *
     * @param suffixPathDirectorySaved a suffix to identify where to find the saved-output to
     *     compare against
     * @throws OperationFailedException
     */
    private void testOnTask(String suffixPathDirectorySaved) throws OperationFailedException {

        try {
            ProvidesStackInput input = MultiInputFixture.createInput(taskFixture.energyStack());

            ExportFeatures<ProvidesStackInput, FeatureList<FeatureInputStack>, FeatureInputStack>
                    task = taskFixture.createTask();

            // The saved results were recorded before certain CSV style options became the default,
            // so we switch back to the old settings for comparison.
            task.setStyle(new ExportFeaturesStyle(false, false, false));

            boolean additionalOutputs = false;

            new ExecuteTaskHelper()
                    .assertExpectedTaskOutputs(
                            input,
                            task,
                            directory,
                            RELATIVE_PATH_SAVED_RESULTS + suffixPathDirectorySaved,
                            ExportOutputter.outputsToCompare(
                                    additionalOutputs,
                                    StackAsProviderFixture.IDENTIFIER + "_",
                                    additionalOutputs,
                                    additionalOutputs));
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
