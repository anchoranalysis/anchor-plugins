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
import java.nio.file.Path;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.out.TiffWriter;
import ome.xml.model.enums.PixelType;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;

/**
 * Writes a stack to the filesystem as a TIFF using the <a
 * href="https://www.openmicroscopy.org/bio-formats/">Bioformats</a> library.
 *
 * @author Owen Feehan
 */
public class BioformatsWriter extends RasterWriter {

    @Override
    public void writeStack(
            Stack stack, Path filePath, boolean makeRGB, RasterWriteOptions writeOptions)
            throws RasterIOException {

        if (stack.allChannelsHaveType(UnsignedByteVoxelType.INSTANCE)) {
            writeByte((Stack) stack, filePath, makeRGB);
        } else {
            throw new RasterIOException(
                    "Channels in stack are not homogenously unsigned 8-bit (byte). Other combinations unsupported");
        }
    }

    // A default extension
    @Override
    public String fileExtension(RasterWriteOptions writeOptions) {
        return "tif";
    }

    @Override
    public void writeStackSeries(
            StackSeries stackSeries,
            Path filePath,
            boolean makeRGB,
            RasterWriteOptions writeOptions)
            throws RasterIOException {
        throw new RasterIOException(
                "Writing time-series is unsupported by this " + RasterWriter.class.getSimpleName());
    }

    private void writeByte(Stack stack, Path filePath, boolean makeRGB) throws RasterIOException {

        if (!(stack.getNumberChannels() == 1 || stack.getNumberChannels() == 3)) {
            throw new RasterIOException("Stack must have 1 or 3 channels");
        }

        try (IFormatWriter writer = createWriter()) {

            writer.setMetadataRetrieve(
                    MetadataUtilities.createMetadata(
                            stack.dimensions(),
                            stack.getNumberChannels(),
                            PixelType.UINT8,
                            makeRGB,
                            false));
            writer.setInterleaved(false);
            writer.setId(filePath.toString());

            if (!writer.canDoStacks() && stack.dimensions().z() > 1) {
                throw new RasterIOException("The writer must support stacks for Z > 1");
            }

            if (makeRGB && stack.getNumberChannels() == 3) {
                writeRGB(writer, stack);
            } else {
                writeSeparateChannel(writer, stack);
            }

        } catch (IOException | FormatException | ServiceException | DependencyException e) {
            throw new RasterIOException(e);
        }
    }

    private void writeRGB(IFormatWriter writer, Stack stack) throws RasterIOException {

        Channel channelRed = stack.getChannel(0);
        Channel channelGreen = stack.getChannel(1);
        Channel channelBlue = stack.getChannel(2);

        int capacity = channelRed.voxels().any().extent().volumeXY();
        int capacityTimesThree = capacity * 3;

        stack.dimensions()
                .extent()
                .iterateOverZ(
                        z -> {
                            try {
                                UnsignedByteBuffer merged =
                                        UnsignedByteBuffer.allocate(capacityTimesThree);
                                putSlice(merged, channelRed, z);
                                putSlice(merged, channelGreen, z);
                                putSlice(merged, channelBlue, z);

                                writer.saveBytes(z, merged.array());
                            } catch (FormatException | IOException e) {
                                throw new RasterIOException(e);
                            }
                        });
    }

    private IFormatWriter createWriter() throws RasterIOException {
        try {
            TiffWriter writer = new TiffWriter();
            // COMPRESSION CURRENTLY DISABLED
            writer.setCompression("LZW");
            writer.setBigTiff(false);
            writer.setValidBitsPerPixel(8);
            writer.setWriteSequentially(true);
            return writer;
        } catch (FormatException e) {
            throw new RasterIOException(e);
        }
    }

    private static void putSlice(UnsignedByteBuffer merged, Channel channel, int z) {
        merged.put(channel.voxels().asByte().sliceBuffer(z));
    }

    private static void writeSeparateChannel(IFormatWriter writer, Stack stack)
            throws FormatException, IOException {

        int sliceIndex = 0;
        for (int channelIndex = 0; channelIndex < stack.getNumberChannels(); channelIndex++) {
            Channel channel = stack.getChannel(channelIndex);
            Voxels<UnsignedByteBuffer> voxels = channel.voxels().asByte();

            for (int z = 0; z < stack.dimensions().z(); z++) {
                writer.saveBytes(sliceIndex++, voxels.sliceBuffer(z).array());
            }
        }
    }
}
