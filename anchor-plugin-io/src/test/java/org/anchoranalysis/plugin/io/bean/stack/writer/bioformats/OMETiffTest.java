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
import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.io.bioformats.bean.writer.OMETiff;
import org.anchoranalysis.test.image.rasterwriter.comparison.ComparisonPlan;
import org.junit.Test;

/**
 * Creates <a href="https://docs.openmicroscopy.org/ome-model/5.6.3/ome-tiff/">OME-TIFF</a> files of
 * various types, and checks they are identical to previously-saved copies in resources.
 *
 * <p>Note that {@link ComparisonPlan#ComparisonPlan(boolean, Optional, boolean, String)} can be
 * used to quickly created the saved copies in the resources.
 *
 * @author Owen Feehan
 */
public class OMETiffTest extends OMETestBase {

    /**
     * The plan on how to compare images.
     *
     * <p>Do not do a byewiseComparison as the OME-TIFF files contain differences each time a new
     * file is produced. Unsure why, perhaps connected with UUIDs?
     */
    private static final ComparisonPlan COMPARISON_PLAN =
            new ComparisonPlan(false, Optional.empty(), false);

    public OMETiffTest() {
        super(ImageFileFormat.OME_TIFF, COMPARISON_PLAN);
    }

    @Test
    public void testThreeChannelsRGBUnsignedByte() throws ImageIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedByteVoxelType.INSTANCE);
    }

    @Test
    public void testThreeChannelsRGBUnsignedShort() throws ImageIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedShortVoxelType.INSTANCE);
    }

    @Override
    protected StackWriter createWriter() {
        return new OMETiff();
    }
}
