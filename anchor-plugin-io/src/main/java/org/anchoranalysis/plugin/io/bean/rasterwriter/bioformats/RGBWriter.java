package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.stack.Stack;
import loci.formats.IFormatWriter;

abstract class RGBWriter {
    
    protected IFormatWriter writer;
    protected Channel channelRed;
    protected Channel channelBlue;
    protected Channel channelGreen;
    
    public RGBWriter(IFormatWriter writer, Stack stack) {
        this.writer = writer;
        this.channelRed = stack.getChannel(0);
        this.channelGreen = stack.getChannel(1);
        this.channelBlue = stack.getChannel(2);
    }

    public void writeAsRGB() throws RasterIOException {

        int capacity = channelRed.voxels().any().extent().volumeXY();

        channelRed
                .extent()
                .iterateOverZ(
                        z -> mergeSliceAsRGB(z, capacity));
    }
    
    protected abstract void mergeSliceAsRGB(int z, int capacity) throws RasterIOException;
}