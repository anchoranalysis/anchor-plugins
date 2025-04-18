/*-
 * #%L
 * anchor-plugin-opencv
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

package org.anchoranalysis.plugin.opencv.bean.feature;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.part.CalculationPart;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.spatial.SizeXY;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.feature.input.FeatureInputStack;
import org.anchoranalysis.plugin.opencv.convert.ConvertToMat;
import org.anchoranalysis.spatial.box.Extent;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

/**
 * Calculates the entire HOG descriptor for an image
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateHOGDescriptor extends CalculationPart<float[], FeatureInputStack> {

    /**
     * Optionally resizes the image before calculating the descriptor (useful for achieving
     * constant-sized descriptors for different sized images)
     */
    private final Optional<SizeXY> resizeTo;

    /** Parameters for the HOG-calculation */
    private final HOGParameters parameters;

    @Override
    protected float[] execute(FeatureInputStack input) throws FeatureCalculationException {
        try {
            Stack stack = extractStack(input.getEnergyStackRequired());
            checkSize(stack.extent());
            return extractHOGDescriptor(stack);

        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private float[] extractHOGDescriptor(Stack stack) throws OperationFailedException {
        try {
            Mat img = ConvertToMat.fromStack(stack);
            MatOfFloat descriptorValues = new MatOfFloat();
            parameters.createDescriptor(stack.extent()).compute(img, descriptorValues);
            return convertToArray(descriptorValues);
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Extracts a stack (that is maybe resized)
     *
     * @throws OperationFailedException
     */
    private Stack extractStack(EnergyStack energyStack) throws OperationFailedException {

        // We can rely that an energy stack always exists
        Stack stack = energyStack.withoutParameters().asStack();

        if (resizeTo.isPresent()) {
            SizeXY size = resizeTo.get();
            return stack.mapChannel(
                    channel ->
                            channel.resizeXY(
                                    size.getWidth(),
                                    size.getHeight(),
                                    parameters.getInterpolator().voxelsResizer()));
        } else {
            return stack;
        }
    }

    private void checkSize(Extent extent) throws FeatureCalculationException {
        if (extent.z() > 1) {
            throw new FeatureCalculationException(
                    "The image is 3D, but the feture only supports 2D images");
        }
        parameters.checkSize(extent);
    }

    private static float[] convertToArray(MatOfFloat mat) {
        float[] arr = new float[mat.rows()];
        mat.get(0, 0, arr);
        return arr;
    }
}
