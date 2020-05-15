package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.FeatureSingleMemo;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.MarkAbstractRadii;
import org.anchoranalysis.feature.cache.SessionInput;

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

import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;

public class BBoxRatio extends FeatureSingleMemo {

	@Override
	public double calc(SessionInput<FeatureInputSingleMemo> input) throws FeatureCalcException {
		
		MarkAbstractRadii markCast = (MarkAbstractRadii) input.get().getPxlPartMemo().getMark();
		
		ImageDim dim = input.get().getDimensionsRequired();
		
		BoundingBox bb = markCast.bbox(dim, GlobalRegionIdentifiers.SUBMARK_INSIDE );
		bb.extnt().setZ( (int) (bb.extnt().getZ() * dim.getRes().getZRelRes()) );
		
		int[] extnt = bb.extnt().createOrderedArray();
		
		int len = extnt.length;
		assert(len>=2);
		
		if (len==2) {
			return ((double) extnt[1]) / extnt[0];
		} else {
			return ((double) extnt[2]) / extnt[0];
		}
	}

}