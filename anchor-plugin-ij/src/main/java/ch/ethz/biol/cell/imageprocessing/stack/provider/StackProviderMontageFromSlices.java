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

package ch.ethz.biol.cell.imageprocessing.stack.provider;

import ij.ImagePlus;
import ij.plugin.MontageMaker;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.stack.StackProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.stack.Stack;

public class StackProviderMontageFromSlices extends StackProviderOne {

    // START BEAN PROPERTIES
    /**
     * How many columns to use in the montage, or 0 to guess an approximately square output
     *
     * <p>The number of rows is automatically calculated.
     */
    @BeanField @Getter @Setter private int columns = 0;

    /** Whether to increase or reduce the size of the images */
    @BeanField @Getter @Setter private double scale = 1;

    /** First slice. If negative, disabled and set to minimum. */
    @BeanField @Getter @Setter private int sliceFirst = -1;

    /** Last slice. If negative, disabled and set to maximum. */
    @BeanField @Getter @Setter private int sliceLast = -1;

    /** Adds a border around each part of the montage */
    @BeanField @Getter @Setter private int borderWidth = 0;

    /** If true a label is added beside every image showing the slice index */
    @BeanField @Getter @Setter private boolean label = false;
    // END BEAN PROPERTIES

    @Override
    public Stack createFromStack(Stack stack) throws CreateException {

        int numSlices = stack.getDimensions().getZ();

        int numColumns = calcNumColumns(numSlices);

        try {
            return stack.mapChnl(
                    chnl ->
                            montageChnl(
                                    chnl,
                                    calcEffectiveColumns(numSlices, numColumns),
                                    calcRowsForColumns(numSlices, numColumns),
                                    calcFirstSlice(),
                                    calcLastSlice(numSlices)));
        } catch (OperationFailedException e) {
            throw new CreateException("Failed to execute map operation on a particular channel", e);
        }
    }

    private Channel montageChnl(
            Channel in, int colsCalc, int rowsCalc, int firstSliceCalc, int lastSliceCalc) {

        ImagePlus imp = IJWrap.createImagePlus(in);

        MontageMaker mm = new MontageMaker();
        ImagePlus res =
                mm.makeMontage2(
                        imp,
                        colsCalc,
                        rowsCalc,
                        scale,
                        firstSliceCalc,
                        lastSliceCalc,
                        1,
                        borderWidth,
                        label);
        return IJWrap.chnlFromImagePlus(res, in.getDimensions().getRes());
    }

    private int calcNumColumns(int totalNumSlices) {
        if (columns > 0) {
            return columns;
        } else {
            // Rounding-up favours a square appearance with some empty cells in the botom
            return (int) Math.ceil(Math.sqrt(totalNumSlices));
        }
    }

    private int calcFirstSlice() {
        // ImageJ's slice indexing begins at 1, so we add 1 to our zero-based indexing
        if (sliceFirst >= 0) {
            return sliceFirst + 1;
        } else {
            return 1;
        }
    }

    private int calcLastSlice(int totalNumSlices) {
        // ImageJ's slice indexing begins at 1, so we add 1 to our zero-based indexing
        if (sliceLast >= 0) {
            return sliceLast + 1;
        } else {
            return totalNumSlices;
        }
    }

    // Possibly corrects if there are more columsn than slices
    private static int calcEffectiveColumns(int totalNumSlices, int columns) {
        return Math.min(totalNumSlices, columns);
    }

    private static int calcRowsForColumns(int totalNumSlices, int columns) {
        return (int) Math.ceil(((double) totalNumSlices) / columns);
    }
}
