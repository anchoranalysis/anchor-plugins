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

package org.anchoranalysis.plugin.io.bean.stack.writer;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.writer.StackWriter;
import org.anchoranalysis.image.io.stack.output.StackWriteOptions;
import org.anchoranalysis.plugin.io.xml.ResolutionAsXML;

/**
 * When writing a stack, an additional file is written to indicate the physical voxel sizes, if this
 * information is known.
 *
 * <p>The path of this file is the raster-path with .xml appended, e.g. {@code
 * rasterFilename.tif.xml}.
 *
 * <p>It contains physical extents of a single voxel (the resolution).
 *
 * <p>This file will only be present if the physical voxel sizes are known, otherwise no file is
 * written.
 *
 * @author Owen Feehan
 */
public class WriteResolutionXml extends StackWriter {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private StackWriter writer;
    // END BEAN PROPERTIES

    @Override
    public ImageFileFormat fileFormat(StackWriteOptions writeOptions) throws ImageIOException {
        return writer.fileFormat(writeOptions);
    }

    @Override
    public void writeStack(Stack stack, Path filePath, StackWriteOptions options)
            throws ImageIOException {
        writer.writeStack(stack, filePath, options);
        writeResolutionXml(filePath, stack.resolution());
    }

    private void writeResolutionXml(Path filePath, Optional<Resolution> resolution)
            throws ImageIOException {
        if (resolution.isPresent()) {
            Path pathOut = NonImageFileFormat.XML.buildPath(filePath);
            ResolutionAsXML.writeResolutionXML(resolution.get(), pathOut);
        }
    }
}
