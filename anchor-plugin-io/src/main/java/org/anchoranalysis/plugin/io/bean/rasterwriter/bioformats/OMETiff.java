package org.anchoranalysis.plugin.io.bean.rasterwriter.bioformats;

import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import loci.formats.IFormatWriter;
import loci.formats.out.OMETiffWriter;

/**
 * Writes a stack to the filesystem as a OME-XML using the <a
 * href="https://www.openmicroscopy.org/bio-formats/">Bioformats</a> library.
 *
 * <p>This is particularly useful for stacks of images that have an unusual
 * number of channels (neither 1 or 3 channels), and which most other file formats
 * cannot support.
 * 
 * @author Owen Feehan
 */
public class OMETiff extends BioformatsWriter {

    @Override
    public String fileExtension(RasterWriteOptions writeOptions) {
        return "ome.tif";
    }

    @Override
    protected IFormatWriter createWriter() throws RasterIOException {
        return new OMETiffWriter();
    }
}
