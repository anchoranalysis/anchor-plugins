package ch.ethz.biol.cell.mpp.nrg.feature.all;

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

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputAllMemo;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoCollection;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CalculateDeriveMarkInput extends FeatureCalculation<FeatureInputMark, FeatureInputAllMemo> {

	@Override
	protected FeatureInputMark execute(FeatureInputAllMemo params) throws FeatureCalcException {
		
		MemoCollection list = params.getPxlPartMemo();
		
		if (list.size()==0) {
			throw new FeatureCalcException("No mark exists in the list");
		}
		
		if (list.size()>1) {
			throw new FeatureCalcException("More than one mark exists in the list");
		}
		
		Mark mark = list.getMemoForIndex(0).getMark();
		
		return new FeatureInputMark(
			mark,
			params.getResOptional()
		);	
	}
	
	@Override
	public boolean equals(Object other) {
		return (other instanceof CalculateDeriveMarkInput);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().toHashCode();
	}
}
