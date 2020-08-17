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

import java.io.IOException;
import java.nio.ByteBuffer;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;

// Writes a stack to the file system in some manner
public class OMEXMLWriter extends ByteNoTimeSeriesWriter {

    // A default extension
    @Override
    public String defaultExtension() {
        return "ome";
    }

    @Override
    protected IFormatWriter createWriter() throws RasterIOException {
        return new ImageWriter();
    }

    @Override
    protected void writeRGB(IFormatWriter writer, Stack stack)
            throws FormatException, IOException, RasterIOException {

        Channel chnlRed = stack.getChannel(0);
        Channel chnlGreen = stack.getChannel(1);
        Channel chnlBlue = stack.getChannel(2);

        Voxels<ByteBuffer> voxelsRed = chnlRed.voxels().asByte();
        Voxels<ByteBuffer> voxelsGreen = chnlGreen.voxels().asByte();
        Voxels<ByteBuffer> voxelsBlue = chnlBlue.voxels().asByte();

        for (int z = 0; z < stack.dimensions().z(); z++) {

            ByteBuffer red = voxelsRed.sliceBuffer(z);
            ByteBuffer green = voxelsGreen.sliceBuffer(z);
            ByteBuffer blue = voxelsBlue.sliceBuffer(z);

            ByteBuffer merged =
                    ByteBuffer.allocate(red.capacity() + green.capacity() + blue.capacity());
            merged.put(red);
            merged.put(green);
            merged.put(blue);

            writer.saveBytes(z, merged.array());
        }
    }
}
