/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.comparer.Comparer;
import org.anchoranalysis.annotation.io.wholeimage.findable.Findable;
import org.anchoranalysis.annotation.io.wholeimage.findable.Found;
import org.anchoranalysis.annotation.io.wholeimage.findable.NotFound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReaderUtilities;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;

public class BinaryChnlComparer extends Comparer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathGenerator filePathGenerator;

    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    @BeanField @Getter @Setter private boolean invert = false;
    // END BEAN PROPERTIES

    @Override
    public Findable<ObjectCollection> createObjects(
            Path filePathSource, ImageDimensions dimensions, boolean debugMode)
            throws CreateException {

        try {
            Path maskPath = filePathGenerator.outFilePath(filePathSource, debugMode);

            if (!maskPath.toFile().exists()) {
                return new NotFound<>(maskPath, "No mask exists");
            }

            Mask chnl =
                    RasterReaderUtilities.openBinaryChnl(
                            rasterReader, maskPath, createBinaryValues());

            return new Found<>(convertToObjects(chnl));

        } catch (AnchorIOException | RasterIOException e) {
            throw new CreateException(e);
        }
    }

    private BinaryValues createBinaryValues() {
        if (invert) {
            return BinaryValues.getDefault().createInverted();
        } else {
            return BinaryValues.getDefault();
        }
    }

    private static ObjectCollection convertToObjects(Mask chnl) {
        return ObjectCollectionFactory.from(chnl);
    }
}
