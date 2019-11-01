package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.mark.Mark;

/*-
 * #%L
 * anchor-plugin-points
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

import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ops.ObjMaskMerger;

import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMapSingleton;
import ch.ethz.biol.cell.mpp.mark.regionmap.RegionMembershipWithFlags;

class EllipticityCalculatorHelper {

	public static double calc( ObjMask om, Mark mark, ImageDim dim ) {
		ObjMask omCompare = maskCompare(mark, dim);
		return calc(om, omCompare);
	}
	
	private static double calc( ObjMask om, ObjMask omCompare ) {
		ObjMask omMerge = ObjMaskMerger.merge(om, omCompare );
		return calcWithMerged(om, omCompare, omMerge);
	}
	
	private static double calcWithMerged( ObjMask om, ObjMask omCompare, ObjMask omMerge ) {
		int numPixelsCompare = omCompare.numPixels();
		int numUnion = omMerge.numPixels();
		
		// Interseting pixels
		int numIntersection = om.countIntersectingPixels(omCompare);
		
		return intDiv(
			numPixelsCompare,
			numUnion - numIntersection + numPixelsCompare
		);
	}
		
	private static ObjMask maskCompare(Mark mark, ImageDim dim) {
		RegionMembershipWithFlags rm = RegionMapSingleton.instance().membershipWithFlagsForIndex(0);
		assert( rm.getRegionID()==0 );
		return mark.calcMask(dim, rm, BinaryValuesByte.getDefault() ).getMask();	
	}
	
	private static double intDiv( int num, int dem ) {
		return ((double) num) / dem;
	}
}
