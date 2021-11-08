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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeatures;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesStyle;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.experiment.task.TaskSingleInputHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the exporting of features.
 *
 * <p>Supports generally any feature type.
 *
 * @author Owen Feehan
 * @param <S> input-type
 * @param <T> feature-type exported
 * @param <U> shared-state type
 * @param <V> type of fixture used for creating tasks
 */
abstract class ExportFeaturesTestBase<
        S extends InputFromManager,
        T extends FeatureInputEnergy,
        U,
        V extends TaskFixture<S, T, U>> {

    private static Path EXPECTED_RESULTS_BASE = Paths.get("expectedOutput");

    private static TestLoader loader;
    protected V taskFixture;

    private String relativePathSavedResults;

    /** If true, a larger set of outputs are expected and compared. */
    private boolean additionalOutputs;

    private CheckedFunction<TestLoader, V, CreateException> fixtureCreator;

    @TempDir Path directory;

    /**
     * Creates with the results in a particular sub-directory.
     *
     * @param subdirectoryExpected the name of the sub-directory where the results will be found
     */
    ExportFeaturesTestBase(
            String subdirectoryExpected,
            boolean additionalOutputs,
            CheckedFunction<TestLoader, V, CreateException> fixtureCreator) {
        relativePathSavedResults =
                EXPECTED_RESULTS_BASE.resolve(subdirectoryExpected).toString() + "/";
        this.fixtureCreator = fixtureCreator;
        this.additionalOutputs = additionalOutputs;
    }

    @BeforeAll
    static void setup() {

        RegisterBeanFactories.registerAllPackageBeanFactories();
        loader = TestLoader.createFromMavenWorkingDirectory();
    }

    @BeforeEach
    void setupTest() throws CreateException {
        taskFixture = fixtureCreator.apply(loader);
    }

    /** Runs a test to check if the results of exporting-features correspond to saved-values. */
    protected void testOnTask(String outputDirectory, Consumer<V> changeFixture)
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
    protected void testOnTask(String suffixPathDirectorySaved) throws OperationFailedException {

        try {
            @SuppressWarnings("unchecked")
            S input = (S) MultiInputFixture.createInput(taskFixture.energyStack());

            ExportFeatures<S, U, T> task = taskFixture.createTask();
            
            // The saved results were recorded before certain CSV style options became the default, so
            // we switch back to the old settings for comparison.
            task.setStyle( new ExportFeaturesStyle(false, false) );

            TaskSingleInputHelper.runTaskAndCompareOutputs(
                    input,
                    task,
                    directory,
                    relativePathSavedResults + suffixPathDirectorySaved,
                    ExportOutputter.outputsToCompare(
                            additionalOutputs, additionalOutputs, additionalOutputs));
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
