package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembershipWithFlags;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.regionmap.RegionMapSingleton;


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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ObjectMaskMerger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class EllipticityCalculatorHelper {

	public static double calc(ObjectMask object, Mark mark, ImageDimensions dimensions) {
		return calc(
			object,
			maskCompare(mark, dimensions)
		);
	}
	
	private static double calc( ObjectMask object, ObjectMask objectCompare ) {
		return calcWithMerged(
			object,
			objectCompare,
			ObjectMaskMerger.merge(object, objectCompare )
		);
	}
	
	private static double calcWithMerged( ObjectMask object, ObjectMask objectCompare, ObjectMask merged ) {
		int numPixelsCompare = objectCompare.numberVoxelsOn();
		int numUnion = merged.numberVoxelsOn();
		
		// Interseting pixels
		int numIntersection = object.countIntersectingVoxels(objectCompare);
		
		return intDiv(
			numPixelsCompare,
			numUnion - numIntersection + numPixelsCompare
		);
	}
		
	private static ObjectMask maskCompare(Mark mark, ImageDimensions dim) {
		RegionMembershipWithFlags rm = RegionMapSingleton.instance().membershipWithFlagsForIndex(0);
		assert( rm.getRegionID()==0 );
		return mark.calcMask(dim, rm, BinaryValuesByte.getDefault() ).getMask();	
	}
	
	private static double intDiv( int num, int dem ) {
		return ((double) num) / dem;
	}
}
