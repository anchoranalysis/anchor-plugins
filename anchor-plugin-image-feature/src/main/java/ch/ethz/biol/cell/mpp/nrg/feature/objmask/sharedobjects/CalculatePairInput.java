package ch.ethz.biol.cell.mpp.nrg.feature.objmask.sharedobjects;

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

import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculatePairInput extends FeatureCalculation<FeatureInputPairObjs, FeatureInputSingleObj> {

	private BinaryChnl chnl;
		
	public CalculatePairInput(BinaryChnl chnl) {
		super();
		this.chnl = chnl;
	}

	@Override
	protected FeatureInputPairObjs execute(FeatureInputSingleObj input) throws FeatureCalcException {

		ObjectMask objFromBinary = new ObjectMask(
			chnl.binaryVoxelBox()
		);
		
		return new FeatureInputPairObjs(
			input.getObjMask(),
			objFromBinary,
			input.getNrgStackOptional()
		);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof CalculatePairInput;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
