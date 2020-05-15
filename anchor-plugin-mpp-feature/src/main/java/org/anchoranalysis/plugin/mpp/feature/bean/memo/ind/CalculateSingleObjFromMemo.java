package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

/*-
 * #%L
 * anchor-plugin-mpp-feature
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

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.properties.ObjMaskWithProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class CalculateSingleObjFromMemo extends FeatureCalculation<FeatureInputSingleObj, FeatureInputSingleMemo> {

	private RegionMap regionMap;
	private int index;
		
	public CalculateSingleObjFromMemo(RegionMap regionMap, int index) {
		super();
		this.regionMap = regionMap;
		this.index = index;
	}

	@Override
	protected FeatureInputSingleObj execute(FeatureInputSingleMemo input) throws FeatureCalcException {
		return new FeatureInputSingleObj(
			calcMask(input),
			input.getNrgStackOptional()
		);
	}
	
	private ObjMask calcMask(FeatureInputSingleMemo params) throws FeatureCalcException {
		ObjMaskWithProperties om = params.getPxlPartMemo().getMark().calcMask(
			params.getDimensionsRequired(),
			regionMap.membershipWithFlagsForIndex(index),
			BinaryValuesByte.getDefault()
		);
		return om.getMask();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}
		CalculateSingleObjFromMemo rhs = (CalculateSingleObjFromMemo) obj;
		return new EqualsBuilder()
             .append(regionMap, rhs.regionMap)
             .append(index, rhs.index)
             .isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(index)
			.append(regionMap)
			.toHashCode();
	}
}
