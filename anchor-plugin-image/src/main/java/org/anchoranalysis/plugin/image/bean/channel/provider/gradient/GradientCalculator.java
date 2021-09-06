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

package org.anchoranalysis.plugin.image.bean.channel.provider.gradient;

import java.nio.FloatBuffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.convert.imglib2.ConvertToImg;

/**
 * Calculates the gradient in one or more dimensions.
 * 
 * <p>An out-of-bounds strategy of <i>mirror</i> is used for voxel's lying at the boundary.
 *
 * @author Owen Feehan
 */
@RequiredArgsConstructor
class GradientCalculator {

    // START REQUIRED ARGUMENTS
    /**
     * a boolean array indicating whether a dimension (X,Y,Z) should be included in the calculation
     */
    private final boolean[] dimensions;

    /** the gradient is multiplied by this constant in the output */
    private final float scaleFactor;

    /**
     * adds a constant after scale-factor has been applied (useful for shifting negative numbers
     * into a positive range)
     */
    private final int addSum;
    // END REQUIRED ARGUMENTS

    /** calculate central-difference instead of backward-difference (finite differences) */
    @Getter @Setter private boolean centralDifference = false;

    /**
     * iff true, we apply a l2norm to our difference (useful for getting magnitude if working with
     * more than 1 dimension)?
     */
    @Getter @Setter private boolean norm = true;

    /**
     * Calculates the gradient of a channel
     *
     * @param signalIn where to calculate gradient from
     * @param gradientOut where to output the gradient to
     */
    public void gradient(VoxelsWrapper signalIn, Voxels<FloatBuffer> gradientOut) {
        gradientImgLib2(
                ConvertToImg.from(signalIn), // Input channel
                ConvertToImg.fromFloat(gradientOut) // Output channel
                );
    }

    /**
     * Uses ImgLib2 to calculate gradient
     *
     * <p>See <a
     * href="https://github.com/imglib/imglib2-algorithm-gpl/blob/master/src/main/java/net/imglib2/algorithm/pde/Gradient.java>ImgLib2</a>
     *
     * @param input input-image
     * @param output output-image
     */
    private void gradientImgLib2(Img<? extends RealType<?>> input, Img<FloatType> output) {

        Cursor<? extends RealType<?>> in = Views.iterable(input).localizingCursor();
        RandomAccess<FloatType> oc = output.randomAccess();

        OutOfBounds<? extends RealType<?>> ra = Views.extendMirrorDouble(input).randomAccess();

        while (in.hasNext()) {
            in.fwd();

            // Position neighborhood cursor
            ra.setPosition(in);

            // Position output cursor
            for (int i = 0; i < input.numDimensions(); i++) {
                oc.setPosition(in.getLongPosition(i), i);
            }

            double diffSum =
                    processDimensions(
                            ra, input.numDimensions(), in.get().getRealFloat() // central value
                            );

            oc.get().set(calculateOutputPixel(diffSum));
        }
    }

    private double processDimensions(
            OutOfBounds<? extends RealType<?>> ra, int numDims, float central) {

        double diffSum = 0.0;

        for (int i = 0; i < numDims; i++) {

            // Skip any dimension that isn't included
            if (!dimensions[i]) {
                continue;
            }

            ra.fwd(i);
            float diff = central - ra.get().getRealFloat();
            ra.bck(i);

            if (centralDifference) {
                ra.bck(i);
                diff += ra.get().getRealFloat();
                ra.fwd(i);
            }

            if (norm) {
                diffSum += Math.pow(diff, 2.0);
            } else {
                diffSum += diff;
            }
        }
        return diffSum;
    }

    private float calculateOutputPixel(double diffSum) {
        float diffOut = (float) maybeNorm(diffSum);
        return (diffOut * scaleFactor) + addSum;
    }

    private double maybeNorm(double diffSum) {
        if (norm) {
            return Math.sqrt(diffSum);
        } else {
            return diffSum;
        }
    }
}
