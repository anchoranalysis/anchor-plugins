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

package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import java.nio.ByteBuffer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateMaskInput<T extends FeatureInputNRG>
        extends FeatureCalculation<FeatureInputSingleObject, T> {

    private final Mask mask;

    @Override
    protected FeatureInputSingleObject execute(T input) throws FeatureCalculationException {

        BinaryVoxelBox<ByteBuffer> bvb = binaryVoxelBox(mask);

        return new FeatureInputSingleObject(new ObjectMask(bvb), input.getNrgStackOptional());
    }

    private static BinaryVoxelBox<ByteBuffer> binaryVoxelBox(Mask mask)
            throws FeatureCalculationException {
        VoxelBox<ByteBuffer> voxelBox;
        try {
            voxelBox = mask.getChannel().voxels().asByte();
        } catch (IncorrectVoxelDataTypeException e) {
            throw new FeatureCalculationException(
                    "mask has incompatible data type, it must be unsigned 8-bit", e);
        }

        return new BinaryVoxelBoxByte(voxelBox, mask.getBinaryValues());
    }
}
