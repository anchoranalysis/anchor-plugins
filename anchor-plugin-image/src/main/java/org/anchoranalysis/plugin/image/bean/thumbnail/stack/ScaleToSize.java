/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.thumbnail.stack;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.displayer.StackDisplayer;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.DisplayStack;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;

/**
 * Create a thumbnail by scales an image to a particular size.
 *
 * <p>The aspect ratio between width and height is not preserved.
 *
 * <p>If it's a z-stack, a maximum intensity projection is first applied.
 *
 * @author Owen Feehan
 */
public class ScaleToSize extends ThumbnailFromStack {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private SizeXY size = new SizeXY(200, 200);

    @BeanField @Getter @Setter @DefaultInstance private Interpolator interpolator;

    /** How to convert an image to be displayed to the user. */
    @BeanField @Getter @Setter @DefaultInstance private StackDisplayer displayer;

    // END BEAN PROPERTIES

    private VoxelsResizer resizerCreated;

    @Override
    public void start() {
        resizerCreated = interpolator.voxelsResizer();
    }

    @Override
    public DisplayStack thumbnailFor(Stack stack) throws CreateException {

        try {
            Stack resized =
                    stack.extractUpToThreeChannels()
                            .mapChannel(
                                    channel ->
                                            channel.projectMax()
                                                    .resizeXY(
                                                            size.getWidth(),
                                                            size.getHeight(),
                                                            resizerCreated));

            return displayer.deriveFrom(resized);

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
