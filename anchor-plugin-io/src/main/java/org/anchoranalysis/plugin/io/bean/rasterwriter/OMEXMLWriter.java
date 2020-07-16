/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.ImageWriter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// Writes a stack to the file system in some manner
public class OMEXMLWriter extends ByteNoTimeSeriesWriter {

    // A default extension
    @Override
    public String dfltExt() {
        return "ome";
    }

    @Override
    protected IFormatWriter createWriter() throws RasterIOException {
        return new ImageWriter();
    }

    @Override
    protected void writeRGB(IFormatWriter writer, Stack stack)
            throws FormatException, IOException, RasterIOException {

        Channel chnlRed = stack.getChnl(0);
        Channel chnlGreen = stack.getChnl(1);
        Channel chnlBlue = stack.getChnl(2);

        VoxelBox<ByteBuffer> vbRed = chnlRed.getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbGreen = chnlGreen.getVoxelBox().asByte();
        VoxelBox<ByteBuffer> vbBlue = chnlBlue.getVoxelBox().asByte();

        for (int z = 0; z < stack.getDimensions().getZ(); z++) {

            ByteBuffer red = vbRed.getPixelsForPlane(z).buffer();
            ByteBuffer green = vbGreen.getPixelsForPlane(z).buffer();
            ByteBuffer blue = vbBlue.getPixelsForPlane(z).buffer();

            ByteBuffer merged =
                    ByteBuffer.allocate(red.capacity() + green.capacity() + blue.capacity());
            merged.put(red);
            merged.put(green);
            merged.put(blue);

            writer.saveBytes(z, merged.array());
        }
    }
}
