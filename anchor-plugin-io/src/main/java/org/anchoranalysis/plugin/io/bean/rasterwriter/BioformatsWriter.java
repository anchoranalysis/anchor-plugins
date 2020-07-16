/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import loci.formats.FormatException;
import loci.formats.IFormatWriter;
import loci.formats.out.TiffWriter;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;

// Writes a stack to the file system in some manner
public class BioformatsWriter extends ByteNoTimeSeriesWriter {

    // A default extension
    @Override
    public String dfltExt() {
        return "tif";
    }

    @Override
    protected IFormatWriter createWriter() throws RasterIOException {
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

    @Override
    protected void writeRGB(IFormatWriter writer, Stack stack)
            throws FormatException, IOException, RasterIOException {

        Channel chnlRed = stack.getChnl(0);
        Channel chnlGreen = stack.getChnl(1);
        Channel chnlBlue = stack.getChnl(2);

        int cap = chnlRed.getVoxelBox().any().extent().getVolumeXY();
        int cap3 = cap * 3;

        for (int z = 0; z < stack.getDimensions().getZ(); z++) {

            ByteBuffer red = chnlRed.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
            ByteBuffer green = chnlGreen.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
            ByteBuffer blue = chnlBlue.getVoxelBox().asByte().getPixelsForPlane(z).buffer();

            ByteBuffer merged = ByteBuffer.allocate(cap3);
            merged.put(red);
            merged.put(green);
            merged.put(blue);

            writer.saveBytes(z, merged.array());
        }
    }
}
