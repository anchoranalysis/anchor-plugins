package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import java.nio.file.Path;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import ome.xml.model.enums.PixelType;

/**
 * Base class for writing a stack to the filesystem using the <a
 * href="https://www.openmicroscopy.org/bio-formats/">Bioformats</a> library.
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
        
        if (stack.allChannelsHaveType(UnsignedByteVoxelType.INSTANCE)) {
            writeByte((Stack) stack, filePath, makeRGB);
        } else {
            throw new RasterIOException(
                    "Channels in stack are not homogenously unsigned 8-bit (byte). Other combinations unsupported");
        }
    }
    
    protected static void writeAsSeparateChannels(IFormatWriter writer, Stack stack) throws RasterIOException {
        try {
            int sliceIndex = 0;
            for (int channelIndex = 0; channelIndex < stack.getNumberChannels(); channelIndex++) {
                Channel channel = stack.getChannel(channelIndex);
                Voxels<UnsignedByteBuffer> voxels = channel.voxels().asByte();
    
                for (int z = 0; z < stack.dimensions().z(); z++) {
                    writer.saveBytes(sliceIndex++, voxels.sliceBuffer(z).array());
                }
            }
        } catch (IOException | FormatException e) {
            throw new RasterIOException(e);
        }
    }
    
    protected abstract IFormatWriter createWriter() throws RasterIOException;
    
    private void writeByte(Stack stack, Path filePath, boolean makeRGB) throws RasterIOException {

        try (IFormatWriter writer = createWriter()) {
            
            prepareWriter(writer, stack, PixelType.UINT8, makeRGB);
            
            writer.setId(filePath.toString());
            
            if (!writer.canDoStacks() && stack.dimensions().z() > 1) {
                throw new RasterIOException("The writer must support stacks for Z > 1");
            }

            writeStack(writer, stack, makeRGB);

        } catch (IOException | FormatException | ServiceException | DependencyException e) {
            throw new RasterIOException(e);
        }
    }
    
    private void prepareWriter(IFormatWriter writer, Stack stack, PixelType pixelType, boolean makeRGB) throws ServiceException, DependencyException {
        writer.setMetadataRetrieve(
                MetadataUtilities.createMetadata(
                        stack.dimensions(),
                        stack.getNumberChannels(),
                        pixelType,
                        makeRGB,
                        false));
        writer.setInterleaved(makeRGB);
    }
    
    private void writeStack(IFormatWriter writer, Stack stack, boolean makeRGB) throws RasterIOException {
        if (makeRGB) {
            if (stack.getNumberChannels() == 3) {
                writeAsRGB(writer, stack);
            } else {
                throw new RasterIOException("If makeRGB==true, then a stack must have exactly 3 channels, but it actually has: " + stack.getNumberChannels());
            }
        } else {
            writeAsSeparateChannels(writer, stack);
        }
    }
    
    private void writeAsRGB(IFormatWriter writer, Stack stack) throws RasterIOException {

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
    
    private static void putSlice(UnsignedByteBuffer merged, Channel channel, int z) {
        merged.put(channel.voxels().asByte().sliceBuffer(z));
    }
}
