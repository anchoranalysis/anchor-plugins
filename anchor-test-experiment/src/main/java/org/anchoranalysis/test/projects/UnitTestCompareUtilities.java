/* (C)2020 */
package org.anchoranalysis.test.projects;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.DualComparer;
import org.junit.rules.TemporaryFolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UnitTestCompareUtilities {

    /**
     * As we use a common format for storing our tests for the different modules. We can reuse the
     * same code for executing the experiment
     *
     * <p>This function executes the experiment inplace (not in a temporary folder), and creates a
     * DualComparer, where: 1. the first test-loader is associated with our saved-objects directory
     * 2. the second test-loader is associated with the output directory of the folder
     *
     * @param experimentName the name of the experiment (both the beanXML and in the test folders)
     * @param experimentIdentifierOutput how the experiment is identified in the output folder
     * @return the DualComparer describe aboved
     */
    public static DualComparer execExperiment(
            String experimentName, String experimentIdentifierOutput) {
        TestLoader testLoader = TestLoader.createFromMavenWorkingDirectory();

        String pathTestDataFolder = createPathTestDataFolder(experimentName);

        ExperimentLauncherFromShell launcher = new ExperimentLauncherFromShell(testLoader);

        launcher.runExperiment(
                createPathExperiment(experimentName),
                Optional.of(createPathReplacementInput(pathTestDataFolder)),
                Optional.of(createPathReplacementOutput(pathTestDataFolder)));

        String pathOutput = createPathOutput(experimentIdentifierOutput);
        String pathSavedOutput = createPathSavedOutput(experimentName);

        return new DualComparer(
                testLoader.createForSubdirectory(pathSavedOutput),
                testLoader.createForSubdirectory(pathOutput));
    }

    /**
     * As we use a common format for storing our tests for the different modules. We can reuse the
     * same code for executing the experiment IN A TEMPORARY FOLDER
     *
     * <p>This function executes the experiment in a temporary folder, and creates a DualComparer,
     * where: 1. the first test-loader is associated with our saved-objects directory 2. the second
     * test-loader is associated with the output directoy of the temporary folder
     *
     * @param experimentName the name of the experiment (both the beanXML and in the test folders)
     * @param experimentIdentifierOutput how the experiment is identified in the output folder
     * @param includeShared includes the shared folder that is assumed to exist alongside this
     *     repository
     * @param folderTemp the temporary-folder to copy into
     * @return the DualComparer describe aboved
     */
    public static DualComparer execExperimentInTemporary(
            String experimentName,
            String experimentIdentifierOutput,
            boolean includeShared,
            TemporaryFolder folderTemp) {
        TestLoader testLoader = TestLoader.createFromMavenWorkingDirectory();

        String pathTestDataFolder = createPathTestDataFolder(experimentName);

        ExperimentLauncherFromShell launcher = new ExperimentLauncherFromShell(testLoader);

        String[] subdirectories = selectSubdirectories(pathTestDataFolder, includeShared);

        TestLoader testLoaderTemp =
                launcher.runExperimentInTemporaryFolder(
                        createPathExperiment(experimentName),
                        Optional.of(createPathReplacementInput(pathTestDataFolder)),
                        Optional.of(createPathReplacementOutput(pathTestDataFolder)),
                        folderTemp,
                        subdirectories);

        String pathOutput = createPathOutput(experimentIdentifierOutput);
        String pathSavedOutput = createPathSavedOutput(experimentName);

        return new DualComparer(
                testLoader.createForSubdirectory(pathSavedOutput),
                testLoaderTemp.createForSubdirectory(pathOutput));
    }

    private static String createPathExperiment(String experimentName) {
        return String.format("anchorConfig/Experiments/DNAPipeline/%s.xml", experimentName);
    }

    private static String createPathReplacementInput(String pathTestDataFolder) {
        return String.format("%s/input.xml", pathTestDataFolder);
    }

    private static String createPathReplacementOutput(String pathTestDataFolder) {
        return String.format("%s/outputManager.xml", pathTestDataFolder);
    }

    private static String createPathTestDataFolder(String experimentName) {
        return String.format("testData/input/%s", experimentName);
    }

    private static String createPathOutput(String experimentIdentifierOutput) {
        return String.format("testData/output/%s", experimentIdentifierOutput);
    }

    private static String createPathSavedOutput(String experimentName) {
        return String.format("testData/savedOutput/%s", experimentName);
    }

    /**
     * Creates an array indicating which subdirectories to copy into the temporary folder
     *
     * @param pathTestDataFolder the path to our test data (relative to our test root)
     * @param includeShared whether to include the shared folder or not (assumed to exist at the
     *     location ../../shared relative to the rest root)
     * @return an array of subdirectory paths (relative to the rest root)
     */
    private static String[] selectSubdirectories(String pathTestDataFolder, boolean includeShared) {
        if (includeShared) {
            return new String[] {
                "anchorConfig",
                pathTestDataFolder,
                "shared/anchorConfig" // Assumes that the folder shared exists in correct location
            };
        } else {
            return new String[] {
                "anchorConfig", pathTestDataFolder,
            };
        }
    }
}
