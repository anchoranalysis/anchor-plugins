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
import java.nio.file.Path;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import ome.xml.model.enums.PixelType;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.FloatVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedIntVoxelType;
import org.anchoranalysis.image.voxel.datatype.UnsignedShortVoxelType;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

/**
 * Base class for writing a stack to the filesystem using the <a
 * href="https://www.openmicroscopy.org/bio-formats/">Bioformats</a> library.
 *
 * <p>The following formats are supported with a variety of number of channels and stacks:
 *
 * <ul>
 *   <li>unsigned 8-bit
 *   <li>unsigned 16-bit
 *   <li>unsigned 32-bit
 *   <li>float
 * </ul>
 *
 * <p>Note that when writing as RGB, it insists on three channels, and only supported unsigned 8-bit
 * or unsigned-16 bit as channel types.
 *
 * <p>If a stack has heterogeneous channel types (i.e. not all channels have the same type) then it
 * writes <i>all</i> channels with the most generic type (e.g. most bits).
 *
 * @author Owen Feehan
 */
public abstract class BioformatsWriter extends RasterWriter {

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

    @Override
    public void writeStack(
            Stack stack, Path filePath, boolean makeRGB, RasterWriteOptions writeOptions)
            throws RasterIOException {

        if (stack.getNumberChannels() == 0) {
            throw new RasterIOException("This stack has no channels to write.");
        }

        if (!stack.allChannelsHaveIdenticalType()) {
            throw new RasterIOException(
                    "This writer only supports stacks where all channels have the same data-type.");
        }

        VoxelDataType channelType = stack.getChannel(0).getVoxelDataType();

        if (isChannelTypeSupported(channelType)) {
            writeStackInternal(stack, filePath, makeRGB, pixelTypeFor(channelType));
        } else {
            throw new RasterIOException(
                    "Channels in stack the are an unsupported type: " + channelType);
        }
    }

    protected abstract IFormatWriter createWriter() throws RasterIOException;

    private boolean isChannelTypeSupported(VoxelDataType channelType) {
        return channelType.equals(UnsignedByteVoxelType.INSTANCE)
                || channelType.equals(UnsignedShortVoxelType.INSTANCE)
                || channelType.equals(UnsignedIntVoxelType.INSTANCE)
                || channelType.equals(FloatVoxelType.INSTANCE);
    }

    private void writeStackInternal(
            Stack stack, Path filePath, boolean makeRGB, PixelType pixelType)
            throws RasterIOException {

        try (IFormatWriter writer = createWriter()) {

            prepareWriter(writer, stack, pixelType, makeRGB);

            writer.setId(filePath.toString());

            if (!writer.canDoStacks() && stack.dimensions().z() > 1) {
                throw new RasterIOException("The writer must support stacks for Z > 1");
            }

            writeStack(writer, stack, makeRGB);

        } catch (IOException | FormatException | ServiceException | DependencyException e) {
            throw new RasterIOException(e);
        }
    }

    private void prepareWriter(
            IFormatWriter writer, Stack stack, PixelType pixelType, boolean makeRGB)
            throws ServiceException, DependencyException {
        writer.setMetadataRetrieve(
                MetadataUtilities.createMetadata(
                        stack.dimensions(), stack.getNumberChannels(), pixelType, makeRGB, false));
        writer.setInterleaved(makeRGB);
    }

    private void writeStack(IFormatWriter writer, Stack stack, boolean makeRGB)
            throws RasterIOException {
        if (makeRGB) {
            if (stack.getNumberChannels() != 3) {
                throw new RasterIOException(
                        "If makeRGB==true, then a stack must have exactly 3 channels, but it actually has: "
                                + stack.getNumberChannels());
            } else if (stack.allChannelsHaveType(UnsignedByteVoxelType.INSTANCE)) {
                new RGBWriterByte(writer, stack).writeAsRGB();
            } else if (stack.allChannelsHaveType(UnsignedShortVoxelType.INSTANCE)) {
                new RGBWriterShort(writer, stack).writeAsRGB();
            } else {
                throw new RasterIOException(
                        "If makeRGB==true, then only unsigned 8-bit or unsigned 16-bit voxels are supported");
            }
        } else {
            writeAsSeparateChannels(writer, stack);
        }
    }

    private static void writeAsSeparateChannels(IFormatWriter writer, Stack stack)
            throws RasterIOException {
        try {
            int sliceIndex = 0;
            for (int channelIndex = 0; channelIndex < stack.getNumberChannels(); channelIndex++) {
                Channel channel = stack.getChannel(channelIndex);

                for (int z = 0; z < stack.dimensions().z(); z++) {
                    writer.saveBytes(sliceIndex++, channel.voxels().slice(z).underlyingBytes());
                }
            }
        } catch (IOException | FormatException e) {
            throw new RasterIOException(e);
        }
    }

    private static PixelType pixelTypeFor(VoxelDataType dataType) throws RasterIOException {
        if (dataType.equals(UnsignedByteVoxelType.INSTANCE)) {
            return PixelType.UINT8;
        } else if (dataType.equals(UnsignedShortVoxelType.INSTANCE)) {
            return PixelType.UINT16;
        } else if (dataType.equals(UnsignedIntVoxelType.INSTANCE)) {
            return PixelType.UINT32;
        } else if (dataType.equals(FloatVoxelType.INSTANCE)) {
            return PixelType.FLOAT;
        } else {
            throw new RasterIOException(
                    String.format("%s is an unsupported data-type for this writer", dataType));
        }
    }
}
