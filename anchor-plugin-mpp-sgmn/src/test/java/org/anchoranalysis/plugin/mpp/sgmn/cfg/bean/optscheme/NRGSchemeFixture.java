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

import org.anchoranalysis.anchor.mpp.feature.addcriteria.BBoxIntersection;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.nrg.scheme.NRGSchemeWithSharedFeatures;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.ind.Size;
import org.anchoranalysis.plugin.mpp.feature.bean.memo.pair.overlap.OverlapNumVoxels;
import org.anchoranalysis.plugin.operator.feature.bean.arithmetic.MultiplyByConstant;
import org.anchoranalysis.test.LoggingFixture;

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
	 * @throws CreateException 
	 */
	public static NRGSchemeWithSharedFeatures sizeMinusWeightedOverlap( double weightOverlap ) throws CreateException {
		return new NRGSchemeWithSharedFeatures(
			createNRGScheme(weightOverlap),
			new SharedFeatureMulti(),
			10,
			LoggingFixture.suppressedLogErrorReporter()
		);		
	}
	
	private static NRGScheme createNRGScheme(double weightOverlap) throws CreateException {
		return new NRGScheme(
			FeatureListFactory.from( new Size() ),
			FeatureListFactory.from( new MultiplyByConstant<>(new OverlapNumVoxels(),-1 * weightOverlap) ),
			FeatureListFactory.empty(),
			RegionMapSingleton.instance(),
			new BBoxIntersection()
		);
	}
}
