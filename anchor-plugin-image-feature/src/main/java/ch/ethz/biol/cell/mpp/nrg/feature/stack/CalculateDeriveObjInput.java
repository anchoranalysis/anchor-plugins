package ch.ethz.biol.cell.mpp.nrg.feature.stack;

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
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveObjInput extends FeatureCalculation<FeatureInputSingleObj, FeatureInputStack> {

	private int nrgIndex;
		
	public CalculateDeriveObjInput(int nrgIndex) {
		super();
		this.nrgIndex = nrgIndex;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputStack input) throws FeatureCalcException {
		return new FeatureInputSingleObj(
			extractObjMask(input),
			input.getNrgStackOptional()
		);
	}
	
	private ObjMask extractObjMask(FeatureInputStack input) throws FeatureCalcException {
		
		NRGStackWithParams nrgStack = input.getNrgStackRequired();
		
		Chnl chnl = nrgStack.getChnl(nrgIndex);
		BinaryChnl binary = new BinaryChnl(chnl, BinaryValues.getDefault());
		
		return new ObjMask( binary.binaryVoxelBox() );
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateDeriveObjInput rhs = (CalculateDeriveObjInput) obj;
		return new EqualsBuilder()
             .append(nrgIndex, rhs.nrgIndex)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
