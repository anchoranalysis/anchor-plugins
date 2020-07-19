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

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.conic.RegionMapSingleton;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.properties.ObjectWithProperties;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;
import org.anchoranalysis.image.voxel.statistics.VoxelStatistics;

public class SurfaceSizeMaskNonZero extends FeatureSingleMemoRegion {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int maskIndex = 0;

    @BeanField @Getter @Setter private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField @Getter @Setter private boolean suppressZ = false;
    // END BEAN PROPERTIES

    @Override
    public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalculationException {

        ObjectMask objectMask = createMask(input.get());
        int surfaceSize = estimateSurfaceSize(input.get().getPxlPartMemo(), objectMask);

        return resolveArea(surfaceSize, input.get().getResOptional());
    }

    private ObjectMask createMask(FeatureInputSingleMemo input) throws FeatureCalculationException {
        ObjectWithProperties omWithProps =
                input.getPxlPartMemo()
                        .getMark()
                        .calcMask(
                                input.getDimensionsRequired(),
                                regionMap.membershipWithFlagsForIndex(getRegionID()),
                                BinaryValuesByte.getDefault());
        return omWithProps.getMask();
    }

    private int estimateSurfaceSize(VoxelizedMarkMemo pxlMarkMemo, ObjectMask object)
            throws FeatureCalculationException {

        VoxelBox<ByteBuffer> vbOutline = calcOutline(object, !suppressZ);

        Extent extent = object.getBoundingBox().extent();

        try {
            int size = 0;
            for (int z = 0; z < extent.getZ(); z++) {
                VoxelStatistics stats = pxlMarkMemo.voxelized().statisticsFor(maskIndex, 0, z);
                if (stats.histogram().hasAboveZero()) {
                    size +=
                            vbOutline
                                    .extractSlice(z)
                                    .countEqual(object.getBinaryValues().getOnInt());
                }
            }
            return size;
        } catch (OperationFailedException e) {
            throw new FeatureCalculationException(e);
        }
    }

    private static VoxelBox<ByteBuffer> calcOutline(ObjectMask object, boolean useZ) {
        OutlineKernel3 kernel = new OutlineKernel3(object.getBinaryValuesByte(), false, useZ);
        return ApplyKernel.apply(kernel, object.getVoxelBox(), object.getBinaryValuesByte());
    }
}
