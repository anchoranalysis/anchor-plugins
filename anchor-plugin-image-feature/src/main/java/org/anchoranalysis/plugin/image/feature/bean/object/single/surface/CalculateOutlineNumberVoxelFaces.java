/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.single.surface;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.calculate.FeatureCalculation;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernel;
import org.anchoranalysis.image.voxel.kernel.count.CountKernelNeighborhoodIgnoreOutsideScene;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateOutlineNumberVoxelFaces
        extends FeatureCalculation<Integer, FeatureInputSingleObject> {

    /** Whether to calculate the outline on a MIP */
    private final boolean mip;

    /**
     * Whether to suppress 3D calculations (only consider XY neighbors). Doesn't make sense if
     * mip=TRUE, and will then be ignroed.
     */
    private final boolean suppress3D;

    private static int calculateSurfaceSize(
            ObjectMask object, ImageDimensions dimensions, boolean mip, boolean suppress3D)
            throws OperationFailedException {

        boolean do3D = (dimensions.z() > 1) && !suppress3D;

        if (do3D && mip) {
            // If we're in 3D mode AND MIP mode, then we get a maximum intensity projection
            CountKernel kernel =
                    new CountKernelNeighborhoodIgnoreOutsideScene(
                            false,
                            object.binaryValuesByte(),
                            true,
                            dimensions.extent(),
                            object.boundingBox().cornerMin());

            Voxels<ByteBuffer> voxelsProjected = object.extract().projectMax();
            return ApplyKernel.applyForCount(kernel, voxelsProjected);

        } else {
            CountKernel kernel =
                    new CountKernelNeighborhoodIgnoreOutsideScene(
                            do3D,
                            object.binaryValuesByte(),
                            true,
                            dimensions.extent(),
                            object.boundingBox().cornerMin());
            return ApplyKernel.applyForCount(kernel, object.voxels());
        }
    }

    @Override
    protected Integer execute(FeatureInputSingleObject params) throws FeatureCalculationException {
        try {
            return calculateSurfaceSize(
                    params.getObject(), params.dimensionsRequired(), mip, suppress3D);
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }
}
