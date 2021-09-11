/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.cache.SessionInput;
import org.anchoranalysis.image.core.object.properties.ObjectWithProperties;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.KernelApplicationParameters;
import org.anchoranalysis.image.voxel.kernel.OutsideKernelPolicy;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;
import org.anchoranalysis.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.mpp.bean.regionmap.RegionMapSingleton;
import org.anchoranalysis.mpp.feature.input.FeatureInputSingleMemo;
import org.anchoranalysis.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.spatial.Extent;

public class SurfaceSizeMaskNonZero extends FeatureSingleMemoRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int maskIndex = 0;

    @BeanField @Getter @Setter private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputSingleMemo> input)
            throws FeatureCalculationException {

        ObjectMask objectMask = createMask(input.get());
        int surfaceSize = estimateSurfaceSize(input.get().getPxlPartMemo(), objectMask);

        return resolveArea(surfaceSize, input.get().getResolutionOptional());
    }

    private ObjectMask createMask(FeatureInputSingleMemo input) throws FeatureCalculationException {
        ObjectWithProperties omWithProps =
                input.getPxlPartMemo()
                        .getMark()
                        .deriveObject(
                                input.dimensionsRequired(),
                                regionMap.membershipWithFlagsForIndex(getRegionID()),
                                BinaryValuesByte.getDefault());
        return omWithProps.withoutProperties();
    }

    private int estimateSurfaceSize(VoxelizedMarkMemo pxlMarkMemo, ObjectMask object)
            throws FeatureCalculationException {

        BinaryVoxels<UnsignedByteBuffer> voxelsOutline = outline(object, !suppressZ);

        Extent extent = object.boundingBox().extent();

        try {
            int size = 0;
            for (int z = 0; z < extent.z(); z++) {
                VoxelStatistics stats = pxlMarkMemo.voxelized().statisticsFor(maskIndex, 0, z);
                if (stats.histogram().hasNonZeroCount(1)) {
                    size +=
                            voxelsOutline
                                    .extract()
                                    .slice(z)
                                    .extract()
                                    .voxelsEqualTo(object.binaryValues().getOnInt())
                                    .count();
                }
            }
            return size;
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private static BinaryVoxels<UnsignedByteBuffer> outline(ObjectMask object, boolean useZ) {
        OutlineKernel kernel = new OutlineKernel();
        KernelApplicationParameters params =
                new KernelApplicationParameters(OutsideKernelPolicy.AS_OFF, useZ);
        return ApplyKernel.apply(kernel, object.binaryVoxels(), params);
    }
}
