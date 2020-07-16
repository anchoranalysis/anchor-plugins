/* (C)2020 */
package org.anchoranalysis.plugin.opencv.bean.feature;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.MatConverter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;

/**
 * Calculates the entire HOG descriptor for an image
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateHOGDescriptor extends FeatureCalculation<float[], FeatureInputStack> {

    /**
     * Optionally resizes the image before calculating the descriptor (useful for achiving
     * constant-sized descriptors for different sized images)
     */
    private final Optional<SizeXY> resizeTo;

    /** Parameters for the HOG-calculation */
    private final HOGParameters params;

    @Override
    protected float[] execute(FeatureInputStack input) throws FeatureCalcException {
        try {
            Stack stack = extractStack(input);
            Extent extent = stack.getDimensions().getExtent();

            checkSize(extent);

            Mat img = MatConverter.makeRGBStack(stack);

            MatOfFloat descriptorValues = new MatOfFloat();
            params.createDescriptor(extent).compute(img, descriptorValues);

            return convertToArray(descriptorValues);

        } catch (CreateException | OperationFailedException e) {
            throw new FeatureCalcException(e);
        }
    }

    /**
     * Extracts a stack (that is maybe resized)
     *
     * @throws FeatureCalcException
     * @throws OperationFailedException
     */
    private Stack extractStack(FeatureInputStack input) throws OperationFailedException {

        // We can rely that an NRG stack always exists
        Stack stack = input.getNrgStackOptional().get().getNrgStack().asStack();

        if (resizeTo.isPresent()) {
            SizeXY size = resizeTo.get();
            return stack.mapChnl(chnl -> chnl.resizeXY(size.getWidth(), size.getHeight()));
        } else {
            return stack;
        }
    }

    private void checkSize(Extent extent) throws FeatureCalcException {
        if (extent.getZ() > 1) {
            throw new FeatureCalcException(
                    "The image is 3D, but the feture only supports 2D images");
        }
        params.checkSize(extent);
    }

    private static float[] convertToArray(MatOfFloat mat) {
        float[] arr = new float[mat.rows()];
        mat.get(0, 0, arr);
        return arr;
    }
}
