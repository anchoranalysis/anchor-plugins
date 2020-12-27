/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.bean.stack.writer.bioformats;

import java.io.IOException;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.test.image.rasterwriter.RasterWriterTestBase;
import org.anchoranalysis.test.image.rasterwriter.comparison.ComparisonPlan;
import org.junit.jupiter.api.Test;

/**
 * Base class for {@link StackWriter}s that output the OME file formats.
 *
 * @author Owen Feehan
 */
abstract class OMETestBase extends RasterWriterTestBase {

    private static final VoxelDataType[] SUPPORTED_VOXEL_TYPES =
            RasterWriterTestBase.ALL_SUPPORTED_VOXEL_TYPES;

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }

    /**
     * Creates for a particular extension and types of comparison.
     *
     * @param format the format to be tested and written.
     * @param comparisonPlan what kind of comparisons to execute on the tests.
     */
    public OMETestBase(ImageFileFormat format, ComparisonPlan comparisonPlan) {
        super(format, true, comparisonPlan);
    }

    @Test
    void testSingleChannel() throws ImageIOException, IOException {
        tester.testSingleChannel(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    void testTwoChannels() throws ImageIOException, IOException {
        tester.testTwoChannels(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    void testThreeChannelsSeparate() throws ImageIOException, IOException {
        tester.testThreeChannelsSeparate(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    void testFourChannels() throws ImageIOException, IOException {
        tester.testFourChannels(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    void testThreeChannelsHeterogeneous() throws ImageIOException, IOException {
        tester.testThreeChannelsHeterogeneous();
    }
}
