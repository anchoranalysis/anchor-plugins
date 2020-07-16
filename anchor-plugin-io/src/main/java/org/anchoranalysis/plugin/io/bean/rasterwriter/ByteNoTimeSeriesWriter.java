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
import java.nio.file.Path;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import ome.xml.model.enums.PixelType;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.ImgStackSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.box.VoxelBox;

/**
 * A writer that doesn't support time-series and byte (8-bit) type only
 *
 * @author Owen Feehan
 */
public abstract class ByteNoTimeSeriesWriter extends RasterWriter {

    @Override
    public void writeTimeSeriesStackByte(ImgStackSeries stackSeries, Path filePath, boolean makeRGB)
            throws RasterIOException {
        throw new RasterIOException(
                "Writing time-series is unsupported by this " + RasterWriter.class.getSimpleName());
    }

    @Override
    public void writeStackShort(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {
        throw new RasterIOException(
                "Writing ShortBuffer stack not yet implemented for this writer");
    }

    // Key interface method
    @Override
    public void writeStackByte(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {

        if (!(stack.getNumChnl() == 1 || stack.getNumChnl() == 3)) {
            throw new RasterIOException("Stack must have 1 or 3 channels");
        }

        try (IFormatWriter writer = createWriter()) {

            writer.setMetadataRetrieve(
                    MetadataUtilities.createMetadata(
                            stack.getDimensions(),
                            stack.getNumChnl(),
                            PixelType.UINT8,
                            makeRGB,
                            false));
            writer.setInterleaved(false);
            writer.setId(filePath.toString());

            if (!writer.canDoStacks() && stack.getDimensions().getZ() > 1) {
                throw new RasterIOException("The writer must support stacks for Z > 1");
            }

            if (makeRGB && stack.getNumChnl() == 3) {
                writeRGB(writer, stack);
            } else {
                writeSeperateChnl(writer, stack);
            }

        } catch (IOException | FormatException | ServiceException | DependencyException e) {
            throw new RasterIOException(e);
        }
    }

    protected abstract IFormatWriter createWriter() throws RasterIOException;

    protected abstract void writeRGB(IFormatWriter writer, Stack stack)
            throws FormatException, IOException, RasterIOException;

    private static void writeSeperateChnl(IFormatWriter writer, Stack stack)
            throws FormatException, IOException {

        int cnt = 0;
        for (int c = 0; c < stack.getNumChnl(); c++) {
            Channel chnl = stack.getChnl(c);
            VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();

            for (int z = 0; z < stack.getDimensions().getZ(); z++) {
                writer.saveBytes(cnt++, vb.getPixelsForPlane(z).buffer().array());
            }
        }
    }
}
