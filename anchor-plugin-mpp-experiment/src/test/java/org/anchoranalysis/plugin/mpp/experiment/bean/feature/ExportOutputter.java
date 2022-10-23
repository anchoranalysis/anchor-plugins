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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.plugin.mpp.experiment.bean.feature.task.MultiInputFixture;

/** Helpful routines and constants related to outputting for {@link ExportFeaturesObjectTest}. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExportOutputter {

    // Saved output locations for particular tests
    public static final String OUTPUT_DIRECTORY_SIMPLE_1 = "simple01/";
    public static final String OUTPUT_DIRECTORY_MERGED_1 = "mergedPairs01/";
    public static final String OUTPUT_DIRECTORY_MERGED_2 = "mergedPairs02/";
    public static final String OUTPUT_DIRECTORY_MERGED_3 = "mergedPairs03/";
    public static final String OUTPUT_DIRECTORY_IMAGE_CACHE = "imageCache/";
    public static final String OUTPUT_DIRECTORY_SIMPLE_WITH_REFERENCE = "simpleWithReference/";

    // Used for tests where we expect an exception to be thrown, and thus never to actually be
    // compared
    // It doesn't physically exist
    public static final String OUTPUT_DIRECTORY_IRRELEVANT = "irrelevant/";

    private static final String[] OUTPUTS_TO_COMPARE_ALWAYS = {
        "featuresAggregated.csv", "features.csv", "grouped/arbitraryGroup/featuresGroup.csv",
    };

    /**
     * A list of relative-filenames of outputs to compare.
     *
     * @param inputStack whether to include an input-stack
     * @param energyStack whether to include an energy-stack
     * @param objects whether to include HDF5 objects
     * @return
     */
    public static Iterable<String> outputsToCompare(
            boolean inputStack, String prefix, boolean energyStack, boolean objects) {
        List<String> list = new ArrayList<>();

        Arrays.stream(OUTPUTS_TO_COMPARE_ALWAYS).forEach(list::add);

        if (inputStack) {
            list.add(prefix + "stacks/input.tif");
        }

        if (energyStack) {
        	addEnergyStacks(list, prefix);
        }

        if (objects) {
            list.add(NonImageFileFormat.HDF5.buildPath(prefix + "objects", MultiInputFixture.OBJECTS_NAME));
        }

        return list;
    }
    
    /** Adds the output relative-paths to the energy stacks to {@code list}. */
    private static void addEnergyStacks(List<String> list, String prefix) {
    	for(int index=0; index < 3; index++) {
    		list.add(String.format("%senergyStack/energyStack_0%d.tif", prefix, index));
    	}    	
    }
}
