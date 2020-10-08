package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import java.io.IOException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;

class RGBWriterShort extends RGBWriter {

    public RGBWriterShort(IFormatWriter writer, Stack stack) {
        super(writer, stack);
    }

    @Override
    protected void mergeSliceAsRGB(int z, int capacity) throws RasterIOException {
        UnsignedByteBuffer merged = UnsignedByteBuffer.allocate(capacity*3*2);
        putSliceShort(merged, channelRed, z);
        putSliceShort(merged, channelGreen, z);
        putSliceShort(merged, channelBlue, z);
        try {
            writer.saveBytes(z, merged.array());
        } catch (FormatException | IOException e) {
            throw new RasterIOException(e);
        }  
    }
    
    private static void putSliceShort(UnsignedByteBuffer merged, Channel channel, int z) {
        merged.getDelegate().asShortBuffer().put(channel.voxels().asShort().sliceBuffer(z).getDelegate());
    }        
}