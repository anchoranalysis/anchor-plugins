/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterwriter;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.ImgStackSeries;
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
    @BeanField private RasterWriter writer;
    // END BEAN PROPERTIES

    @Override
    public String dfltExt() {
        return writer.dfltExt();
    }

    @Override
    public void writeStackByte(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writer.writeStackByte(stack, filePath, makeRGB);
        writeResolutionXml(filePath, stack.getDimensions().getRes());
    }

    @Override
    public void writeStackShort(Stack stack, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writer.writeStackShort(stack, filePath, makeRGB);
        writeResolutionXml(filePath, stack.getDimensions().getRes());
    }

    @Override
    public void writeTimeSeriesStackByte(ImgStackSeries stackSeries, Path filePath, boolean makeRGB)
            throws RasterIOException {
        writer.writeTimeSeriesStackByte(stackSeries, filePath, makeRGB);

        // We assume all the stacks in the series have the same dimension, and write only one
        // metadata file
        writeResolutionXml(filePath, stackSeries.get(0).getDimensions().getRes());
    }

    private void writeResolutionXml(Path filePath, ImageResolution res) throws RasterIOException {
        Path pathOut = Paths.get(filePath.toString() + ".xml");
        AnchorMetadataXml.writeResolutionXml(pathOut, res);
    }

    public RasterWriter getWriter() {
        return writer;
    }

    public void setWriter(RasterWriter writer) {
        this.writer = writer;
    }
}
