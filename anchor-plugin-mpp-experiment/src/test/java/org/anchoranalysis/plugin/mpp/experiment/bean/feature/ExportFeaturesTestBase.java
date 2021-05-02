package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import org.anchoranalysis.bean.xml.RegisterBeanFactories;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.feature.input.FeatureInputEnergy;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.test.TestLoader;
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
 * @param <V> type of fixture used for creating tasks
 */
abstract class ExportFeaturesTestBase<
        S extends InputFromManager, T extends FeatureInputEnergy, V extends TaskFixture<S, T, ?>> {

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
            TaskSingleInputHelper.runTaskAndCompareOutputs(
                    createInput(taskFixture.energyStack()),
                    taskFixture.createTask(),
                    directory,
                    relativePathSavedResults + suffixPathDirectorySaved,
                    ExportOutputter.outputsToCompare(
                            additionalOutputs, additionalOutputs, additionalOutputs));
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    protected abstract S createInput(EnergyStackWithoutParams stack);
}
