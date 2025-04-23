/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.stack.provider;

import ij.ImagePlus;
import ij.plugin.MontageMaker;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.stack.StackProviderUnary;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.io.imagej.convert.ConvertFromImagePlus;
import org.anchoranalysis.io.imagej.convert.ConvertToImagePlus;
import org.anchoranalysis.io.imagej.convert.ImageJConversionException;

/**
 * Creates a montage of slices from a stack.
 *
 * <p>This class extends {@link StackProviderUnary} to create a montage of slices from an input
 * stack.
 */
public class MontageSlices extends StackProviderUnary {

    /**
     * How many columns to use in the montage, or 0 to guess an approximately square output.
     *
     * <p>The number of rows is automatically calculated.
     */
    @BeanField @Getter @Setter private int columns = 0;

    /**
     * Whether to increase or reduce the size of the images. A value of 1 maintains the original
     * size.
     */
    @BeanField @Getter @Setter private double scale = 1;

    /** First slice to include in the montage. If negative, set to the first slice. */
    @BeanField @Getter @Setter private int sliceFirst = -1;

    /** Last slice to include in the montage. If negative, set to the last slice. */
    @BeanField @Getter @Setter private int sliceLast = -1;

    /** Adds a border around each part of the montage. */
    @BeanField @Getter @Setter private int borderWidth = 0;

    /** If true, a label is added beside every image showing the slice index. */
    @BeanField @Getter @Setter private boolean label = false;

    @Override
    public Stack createFromStack(Stack stack) throws ProvisionFailedException {

        int numberSlices = stack.dimensions().z();

        int numberColumns = numberColumns(numberSlices);

        try {
            return stack.mapChannel(
                    channel ->
                            montageChannel(
                                    channel,
                                    effectiveColumns(numberSlices, numberColumns),
                                    rowsForColumns(numberSlices, numberColumns),
                                    firstSlice(),
                                    lastSlice(numberSlices)));
        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(
                    "Failed to execute map operation on a particular channel", e);
        }
    }

    /** Creates a montage for a single channel. */
    private Channel montageChannel(
            Channel channel, int columns, int rows, int firstSlice, int lastSlice)
            throws OperationFailedException {

        try {
            ImagePlus imp = ConvertToImagePlus.from(channel);

            MontageMaker mm = new MontageMaker();
            ImagePlus result =
                    mm.makeMontage2(
                            imp,
                            columns,
                            rows,
                            scale,
                            firstSlice,
                            lastSlice,
                            1,
                            borderWidth,
                            label);
            return ConvertFromImagePlus.toChannel(result, channel.dimensions().resolution());
        } catch (ImageJConversionException e) {
            throw new OperationFailedException(e);
        }
    }

    /** Calculates the number of columns for the montage. */
    private int numberColumns(int totalNumSlices) {
        if (columns > 0) {
            return columns;
        } else {
            // Rounding-up favours a square appearance with some empty cells in the botom
            return (int) Math.ceil(Math.sqrt(totalNumSlices));
        }
    }

    /** Determines the first slice to include in the montage. */
    private int firstSlice() {
        // ImageJ's slice indexing begins at 1, so we add 1 to our zero-based indexing
        if (sliceFirst >= 0) {
            return sliceFirst + 1;
        } else {
            return 1;
        }
    }

    /** Determines the last slice to include in the montage. */
    private int lastSlice(int totalNumSlices) {
        // ImageJ's slice indexing begins at 1, so we add 1 to our zero-based indexing
        if (sliceLast >= 0) {
            return sliceLast + 1;
        } else {
            return totalNumSlices;
        }
    }

    /** Corrects the number of columns if there are more columns than slices. */
    private static int effectiveColumns(int totalNumSlices, int columns) {
        return Math.min(totalNumSlices, columns);
    }

    /** Calculates the number of rows needed for the given number of columns and slices. */
    private static int rowsForColumns(int totalNumSlices, int columns) {
        return (int) Math.ceil(((double) totalNumSlices) / columns);
    }
}
