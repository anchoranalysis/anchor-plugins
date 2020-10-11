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
package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import java.util.Optional;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.stack.StackWriter;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.io.bioformats.ConfigureBioformatsLogging;
import org.anchoranalysis.test.image.rasterwriter.RasterWriterTestBase;
import org.junit.Test;

/**
 * Base class for {@link StackWriter}s that output the OME file formats.
 *
 * @author Owen Feehan
 */
public abstract class OMETestBase extends RasterWriterTestBase {

    private static final VoxelDataType[] SUPPORTED_VOXEL_TYPES =
            RasterWriterTestBase.ALL_SUPPORTED_VOXEL_TYPES;

    static {
        ConfigureBioformatsLogging.instance().makeSureConfigured();
    }

    /**
     * Creates for a particular extension and types of comparison.
     *
     * @param extension the extension (without a leading period).
     * @param bytewiseCompare iff true, a bytewise comparison occurs between the saved-file and the
     *     newly created file.
     * @param extensionVoxelwiseCompare iff defined, a voxel-wise comparison occurs with the
     *     saved-rasters from a different extension.
     */
    public OMETestBase(
            String extension, boolean bytewiseCompare, Optional<String> extensionVoxelwiseCompare) {
        super(extension, true, bytewiseCompare, extensionVoxelwiseCompare);
    }

    @Test
    public void testSingleChannel() throws RasterIOException, IOException {
        tester.testSingleChannel(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    public void testTwoChannels() throws RasterIOException, IOException {
        tester.testTwoChannels(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    public void testThreeChannelsSeparate() throws RasterIOException, IOException {
        tester.testThreeChannelsSeparate(SUPPORTED_VOXEL_TYPES);
    }

    @Test
    public void testThreeChannelsRGBUnsignedByte() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedByteVoxelType.INSTANCE);
    }

    @Test
    public void testThreeChannelsRGBUnsignedShort() throws RasterIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedShortVoxelType.INSTANCE);
    }

    @Test
    public void testFourChannels() throws RasterIOException, IOException {
        tester.testFourChannels(SUPPORTED_VOXEL_TYPES);
    }
    
    @Test
    public void testThreeChannelsHeterogeneous() throws RasterIOException, IOException {
        tester.testThreeChannelsHeterogeneous();
    }
}
