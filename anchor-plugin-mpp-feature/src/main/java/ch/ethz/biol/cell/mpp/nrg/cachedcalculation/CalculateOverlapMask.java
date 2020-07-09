package ch.ethz.biol.cell.mpp.nrg.cachedcalculation;

import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputPairMemo;
import org.anchoranalysis.anchor.mpp.overlap.OverlapUtilities;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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
import org.anchoranalysis.image.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor @EqualsAndHashCode(callSuper=false)
public class CalculateOverlapMask extends FeatureCalculation<Double,FeatureInputPairMemo> {

	private final int regionID;
	private final int nrgIndex;
	private final byte maskOnValue;
	
	@Override
	protected Double execute( FeatureInputPairMemo input ) throws FeatureCalcException {
		
		PxlMarkMemo mark1 = input.getObj1();
		PxlMarkMemo mark2 = input.getObj2();
		
		Channel chnl = input.getNrgStackRequired().getNrgStack().getChnl(nrgIndex);
		
		return OverlapUtilities.overlapWithMaskGlobal(
			mark1,
			mark2,
			regionID,
			chnl.getVoxelBox().asByte(),
			maskOnValue
		);
	}
}
