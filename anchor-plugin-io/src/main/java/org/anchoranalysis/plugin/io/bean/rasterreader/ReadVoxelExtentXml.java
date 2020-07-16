/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.rasterreader;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.rasterreader.OpenedRaster;
import org.anchoranalysis.plugin.io.xml.AnchorMetadataXml;

public class ReadVoxelExtentXml extends RasterReader {

    // START BEAN PROPERTIES
    @BeanField private RasterReader rasterReader;

    @BeanField private boolean acceptNoResolution = true;
    // END BEAN PROPERTIES

    /**
     * Looks for a metadata file describing the resolution
     *
     * <p>Given an existing image filepath, the filePath.xml is checked e.g. given
     * /somePath/rasterReader.tif it will look for /somePath/RasterRader.tif.xml
     *
     * @param filepath the filepath of the image
     * @param acceptNoResolution
     * @return the scene res if the metadata file exists and was parsed. null otherwise.
     * @throws RasterIOException
     */
    public static Optional<ImageResolution> readMetadata(Path filepath, boolean acceptNoResolution)
            throws RasterIOException {

        // How we try to open the metadata
        Optional<ImageResolution> res = null;
        File fileMeta = new File(filepath.toString() + ".xml");

        if (fileMeta.exists()) {
            res = Optional.of(AnchorMetadataXml.readResolutionXml(fileMeta));
        } else {
            if (!acceptNoResolution) {
                throw new RasterIOException(
                        String.format("Resolution metadata is required for '%s'", filepath));
            }
        }
        return res;
    }

    @Override
    public OpenedRaster openFile(Path filepath) throws RasterIOException {

        OpenedRaster delegate = rasterReader.openFile(filepath);

        Optional<ImageResolution> sr = readMetadata(filepath, acceptNoResolution);

        return new OpenedRasterAlterDimensions(delegate, res -> sr);
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public void setRasterReader(RasterReader rasterReader) {
        this.rasterReader = rasterReader;
    }

    public boolean isAcceptNoResolution() {
        return acceptNoResolution;
    }

    public void setAcceptNoResolution(boolean acceptNoResolution) {
        this.acceptNoResolution = acceptNoResolution;
    }
}
