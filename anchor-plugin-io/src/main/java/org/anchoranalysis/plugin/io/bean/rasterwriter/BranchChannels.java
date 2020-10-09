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
package org.anchoranalysis.plugin.io.bean.rasterwriter;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;

/**
 * Uses different raster-writers depending on the number/type of channels.
 *
 * <p>If any optional condition does not have a writer, then {@code writer} is used in this case.
 *
 * @author Owen Feehan
 */
public class BranchChannels extends RasterWriterDelegateBase {

    // START BEAN PROPERTIES
    /** Default writer, if a more specific writer is not specified for a condition. */
    @BeanField @Getter @Setter private RasterWriter writer;

    /**
     * Writer employed if a stack is a one or three-channeled image, that is <b>not 3D</b>, and not
     * RGB.
     */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter whenOneOrThreeChannels;

    /** Writer employed if a stack is a three-channeled RGB image and is <b>not 3D</b>. */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter whenRGB;
    // END BEAN PROPERTIES

    @Override
    protected RasterWriter selectDelegate(RasterWriteOptions writeOptions) {
        if (writeOptions.isRgb()) {
            return writerOrDefault(whenRGB);
        } else if (writeOptions.isAlwaysOneOrThreeChannels()) {
            return writerOrDefault(whenOneOrThreeChannels);
        } else {
            return writer;
        }
    }
    
    private RasterWriter writerOrDefault(RasterWriter writerMaybeNull) {
        if (writerMaybeNull!=null) {
            return writerMaybeNull;
        } else {
            return writer;
        }
    }
}
