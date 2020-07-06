package org.anchoranalysis.plugin.image.feature.bean.shared.object;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.nio.ByteBuffer;

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor @EqualsAndHashCode(callSuper=false)
class CalculateBinaryChnlInput<T extends FeatureInputNRG> extends FeatureCalculation<FeatureInputSingleObject, T> {

	private final BinaryChnl chnl;

	@Override
	protected FeatureInputSingleObject execute(T input) throws FeatureCalcException {
		
		BinaryVoxelBox<ByteBuffer> bvb = binaryVoxelBox(chnl);
		
		return new FeatureInputSingleObject(
			new ObjectMask(bvb),
			input.getNrgStackOptional()
		);
	}
	
	private static BinaryVoxelBox<ByteBuffer> binaryVoxelBox( BinaryChnl bic ) throws FeatureCalcException {
		VoxelBox<ByteBuffer> vb;
		try {
			vb = bic.getChnl().getVoxelBox().asByte();
		} catch (IncorrectVoxelDataTypeException e1) {
			throw new FeatureCalcException("binaryImgChnlProvider returned incompatible data type", e1);
		}
		
		return new BinaryVoxelBoxByte( vb, bic.getBinaryValues() );
	}
}
