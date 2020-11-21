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

package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedRaster;
import org.anchoranalysis.plugin.io.xml.ResolutionAsXml;

public class ReadVoxelExtentXml extends StackReader {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private StackReader stackReader;

    @BeanField @Getter @Setter private boolean acceptNoResolution = true;
    // END BEAN PROPERTIES

    /**
     * Looks for a metadata file describing the resolution
     *
     * <p>Given an existing image filepath, the filePath.xml is checked e.g. given
     * /somePath/stackReader.tif it will look for /somePath/RasterRader.tif.xml
     *
     * @param filePath the filepath of the image
     * @param acceptNoResolution
     * @return the scene res if the metadata file exists and was parsed. null otherwise.
     * @throws ImageIOException
     */
    public static Optional<Resolution> readMetadata(Path filePath, boolean acceptNoResolution)
            throws ImageIOException {

        // How we try to open the metadata
        File fileMetadata = NonImageFileFormat.XML.buildPath(filePath).toFile();

        if (fileMetadata.exists()) {
            return Optional.of(ResolutionAsXml.readResolutionXml(fileMetadata));
        } else {
            if (!acceptNoResolution) {
                throw new ImageIOException(
                        String.format("Resolution metadata is required for '%s'", filePath));
            }
            return Optional.empty();
        }
    }

    @Override
    public OpenedRaster openFile(Path path) throws ImageIOException {

        OpenedRaster delegate = stackReader.openFile(path); // NOSONAR

        Optional<Resolution> resolutionToAssign = readMetadata(path, acceptNoResolution);

        return new OpenedRasterAlterDimensions(delegate, existingResolution -> resolutionToAssign);
    }
}
