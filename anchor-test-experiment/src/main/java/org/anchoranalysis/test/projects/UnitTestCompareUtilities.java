/*-
 * #%L
 * anchor-test-experiment
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

package org.anchoranalysis.test.projects;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.DualComparer;
import org.anchoranalysis.test.image.DualComparerFactory;
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
        
        

        String pathTestDataFolder = createPathTestDataFolder(experimentName);

        TestLoader testLoader = TestLoader.createFromMavenWorkingDirectory();
        ExperimentLauncherFromShell launcher = new ExperimentLauncherFromShell(testLoader);

        launcher.runExperiment(
                createPathExperiment(experimentName),
                Optional.of(createPathReplacementInput(pathTestDataFolder)),
                Optional.of(createPathReplacementOutput(pathTestDataFolder)));

        return DualComparerFactory.compareTwoSubdirectoriesInLoader(testLoader, createPathSavedOutput(experimentName), createPathOutput(experimentIdentifierOutput));
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

        return DualComparerFactory.compareTwoSubdirectoriesInLoader(testLoader, pathSavedOutput, testLoaderTemp, pathOutput);
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
