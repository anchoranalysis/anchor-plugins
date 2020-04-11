package ch.ethz.biol.cell.mpp.nrg.feature.all;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureMarkParams;
import org.anchoranalysis.anchor.mpp.feature.mark.MemoMarks;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemAllCalcParams;
import org.anchoranalysis.anchor.mpp.mark.Mark;


/*-
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;

public class AsSingleMark extends FeatureSingleElem<NRGElemAllCalcParams,FeatureMarkParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double calc( CacheableParams<NRGElemAllCalcParams> params ) throws FeatureCalcException {
		return calcCast(
			params.downcastParams(NRGElemAllCalcParams.class)
		);
	}
	
	private double calcCast(CacheableParams<NRGElemAllCalcParams> params) throws FeatureCalcException {
		
		MemoMarks list = params.getParams().getPxlPartMemo();
		
		if (list.size()==0) {
			throw new FeatureCalcException("No mark exists in the list");
		}
		
		if (list.size()>1) {
			throw new FeatureCalcException("More than one mark exists in the list");
		}
		
		Mark mark = list.getMemoForIndex(0).getMark();
		
		return params.calcChangeParams(
			getItem(),
			p -> deriveParams(p, mark),
			"mark"
		);
	}
	
	private static FeatureMarkParams deriveParams( NRGElemAllCalcParams params, Mark mark ) {
		return new FeatureMarkParams(
			mark,
			params.getDimensions().getRes()
		);		
	}
}
