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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.format.NonImageFileFormat;

/** Helpful routines and constants related to outputting for {@link ExportFeaturesTaskTest} */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExportOutputter {

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
    public static final String OUTPUT_DIR_IRRELEVANT = "irrelevant/";

    public static final String[] OUTPUTS_TO_COMPARE = {
        "featuresAggregated.csv",
        "features.csv",
        "grouped/arbitraryGroup/featuresGroup.csv",
        "stacks/input.tif",
        "energyStack/energyStack_00.tif",
        "energyStack/energyStack_01.tif",
        "energyStack/energyStack_02.tif",
        "energyStackParams.xml",
        "grouped/arbitraryGroup/featuresAggregatedGroup.xml",
        NonImageFileFormat.HDF5.buildPath("objects", MultiInputFixture.OBJECTS_NAME),
        "job_manifest.ser.xml"
    };
}
