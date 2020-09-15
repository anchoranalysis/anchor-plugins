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

package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.io.xml.AnchorMetadataXml;

/**
 * When writing a Raster, an additional filename (with .xml appended, e.g. rasterFilename.tif.xml)
 * is also written containing the physical extents of a single voxel (the resolution)
 *
 * @author Owen Feehan
 */
public class WriteResolutionXml extends RasterWriter {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RasterWriter writer;
    // END BEAN PROPERTIES

    @Override
    public String defaultExtension() {
        return writer.defaultExtension();
    }

    @Override
    public void writeStackByte(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writer.writeStackByte(stack, filePath, makeRGB);
        writeResolutionXml(filePath, stack.resolution());
    }

    @Override
    public void writeStackShort(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writer.writeStackShort(stack, filePath, makeRGB);
        writeResolutionXml(filePath, stack.resolution());
    }

    @Override
    public void writeTimeSeriesStackByte(StackSeries stackSeries, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writer.writeTimeSeriesStackByte(stackSeries, filePath, makeRGB);

        // We assume all the stacks in the series have the same dimension, and write only one
        // metadata file
        writeResolutionXml(filePath, stackSeries.get(0).resolution());
    }

    private void writeResolutionXml(Path filePath, Resolution resolution) throws RasterIOException {
        Path pathOut = Paths.get(filePath.toString() + ".xml");
        AnchorMetadataXml.writeResolutionXml(resolution, pathOut);
    }
}
