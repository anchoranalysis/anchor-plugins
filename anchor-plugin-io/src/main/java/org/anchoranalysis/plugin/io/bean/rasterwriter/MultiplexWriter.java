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

import java.nio.file.Path;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.io.generator.raster.series.StackSeries;
import org.anchoranalysis.image.io.rasterwriter.RasterWriteOptions;
import org.anchoranalysis.image.stack.Stack;
import lombok.Getter;
import lombok.Setter;

/**
 * Uses different raster-writers under different sets of conditions.
 * 
 * <p>If any optional condition does not have a writer, then {@code writer} is used in this case.
 * 
 * @author Owen Feehan
 *
 */
public class MultiplexWriter extends RasterWriter {

    /** Default writer, if a more specific writer is not specified for a condition. */
    @BeanField @Getter @Setter private RasterWriter writer;
    
    /** Writer employed if a stack is a one or three-channeled image, that is <b>not 3D</b>, and not RGB. */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter whenOneOrThreeChannels;
    
    /** Writer employed if a stack is a three-channeled RGB image and is <b>not 3D</b>. */
    @BeanField @OptionalBean @Getter @Setter private RasterWriter whenRGB;

    @Override
    public String fileExtension(RasterWriteOptions writeOptions) {
        return selectDelegate(writeOptions).fileExtension(writeOptions);
    }

    @Override
    public void writeStack(Stack stack, Path filePath, boolean makeRGB,
            RasterWriteOptions writeOptions) throws RasterIOException {
        selectDelegate(writeOptions).writeStack(stack, filePath, makeRGB, writeOptions);
    }

    @Override
    public void writeStackSeries(StackSeries stackSeries, Path filePath, boolean makeRGB,
            RasterWriteOptions writeOptions) throws RasterIOException {
        selectDelegate(writeOptions).writeStackSeries(stackSeries, filePath, makeRGB, writeOptions);
    }
    
    private RasterWriter selectDelegate(RasterWriteOptions writeOptions) {
        if (writeOptions.isAlways2D()) {
            
            if (writeOptions.isRgb()) {
                return whenRGB;
            } else if (writeOptions.isAlwaysOneOrThreeChannels()) {
                return whenOneOrThreeChannels;
            } else {
                return writer;
            }
            
        } else {
            return writer;
        }
    }
}
