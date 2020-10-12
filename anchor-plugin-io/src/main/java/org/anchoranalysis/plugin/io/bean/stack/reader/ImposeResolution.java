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

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Resolution;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.stack.OpenedRaster;

public class ImposeResolution extends StackReader {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private StackReader stackReader;

    @BeanField @Getter @Setter private double resX;

    @BeanField @Getter @Setter private double resY;

    @BeanField @Getter @Setter private double resZ = 0.0;

    /** Keep the z-resolution if it is already defined */
    @BeanField @Getter @Setter private boolean keepZ = false;
    // END BEAN PROPERTIES

    @Override
    public OpenedRaster openFile(Path path) throws ImageIOException {
        return new OpenedRasterAlterDimensions(
                stackReader.openFile(path),
                existing -> Optional.of(resolutionToAssign(existing)));
    }
    
    private Resolution resolutionToAssign(Optional<Resolution> existing) throws ImageIOException {
        double z = keepZ && existing.isPresent() ? existing.get().z() : resZ;
        try {
            return new Resolution(resX, resY, z);
        } catch (CreateException e) {
            throw new ImageIOException(e);
        }
    }
}
