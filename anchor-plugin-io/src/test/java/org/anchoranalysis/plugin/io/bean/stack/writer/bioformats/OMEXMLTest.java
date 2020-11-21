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
import org.anchoranalysis.io.bioformats.bean.writer.OMEXML;
import org.anchoranalysis.test.image.rasterwriter.comparison.ComparisonPlan;
import org.junit.Test;

/**
 * Creates <a href="https://docs.openmicroscopy.org/ome-model/5.6.3/ome-xml/">OME-XML</a> files of various types, and checks they are identical to previously-saved copies in resources.
 * 
 * <p>Note that {@link ComparisonPlan#ComparisonPlan(boolean, Optional, boolean, String)} can be used to quickly created the saved copies in the resources.
 * 
 * @author Owen Feehan
 *
 */
public class OMEXMLTest extends OMETestBase {   // NOSONAR

    private static final ComparisonPlan COMPARISON_PLAN = new ComparisonPlan(false, Optional.of(ImageFileFormat.OME_TIFF), false);
    
    public OMEXMLTest() {
        super(ImageFileFormat.OME_XML, COMPARISON_PLAN);
    }
    
    @Test(expected = ImageIOException.class)
    public void testThreeChannelsRGBUnsignedByte() throws ImageIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedByteVoxelType.INSTANCE);
    }

    @Test(expected = ImageIOException.class)
    public void testThreeChannelsRGBUnsignedShort() throws ImageIOException, IOException {
        tester.testThreeChannelsRGB(UnsignedShortVoxelType.INSTANCE);
    }

    @Override
    protected StackWriter createWriter() {
        return new OMEXML();
    }
}
