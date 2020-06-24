package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

import java.util.Arrays;

import org.anchoranalysis.anchor.mpp.feature.addcriteria.BBoxIntersection;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.ind.Size;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap.OverlapNumVoxels;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.test.LoggingFixture;
import org.anchoranalysis.test.bean.BeanTestChecker;

public class NRGSchemeFixture {
	
	private NRGSchemeFixture() {}
	
	/**
	 * An NRG scheme that is rewarded by larger marks but with a penalty for overlap.
	 * 
	 * <div>
	 * Specifically it is:
	 * <code>
	 *  sum(voxels across each mark) - weight * sum(overlapping voxels regions between marks)
	 * </code>
	 * </div>
	 * 
	 * @param weightOverlap a positive integer indicating how much to penalize the overlapping voxels by, the higher the greater the penalty.
	 * @return
	 */
	public static NRGSchemeWithSharedFeatures sizeMinusWeightedOverlap( double weightOverlap ) {
		
		NRGScheme scheme = new NRGScheme();
		scheme.setElemInd(
			Arrays.asList( new Size() )
		);
		scheme.setElemPair(
			Arrays.asList( new MultiplyByConstant<>(new OverlapNumVoxels(),-1 * weightOverlap) )
		);
		
		scheme.setPairAddCriteria( new BBoxIntersection() );
		BeanTestChecker.check(scheme);
		
		return new NRGSchemeWithSharedFeatures(
			scheme,
			new SharedFeatureMulti(),
			10,
			LoggingFixture.suppressedLogErrorReporter()
		);		
	}
}
