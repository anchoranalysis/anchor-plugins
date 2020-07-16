package org.anchoranalysis.plugin.mpp.bean.mark.check;

import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;

/*-
 * #%L
 * anchor-plugin-mpp
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

import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.BoundingBox;

/**
 * The center ps of at least one slice should be on the stack
 * Tries overall center point first, and then projects it onto each slice
 * 
 * @author Owen Feehan
 *
 */
public class AnySliceCenterOnMask extends CheckMarkBinaryChnl {
	
	@Override
	public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack) throws CheckException {
		Point3d cp = mark.centerPoint();
		if(isPointOnBinaryChnl( cp, nrgStack, AnySliceCenterOnMask::derivePoint )) {
			return true;
		}
		
		BoundingBox bbox = mark.bboxAllRegions( nrgStack.getDimensions() ) ;
		ReadableTuple3i cornerMax = bbox.calcCornerMax();
		for( int z=bbox.cornerMin().getZ(); z<=cornerMax.getZ(); z++) {
			Point3d cpSlice = new Point3d(cp.getX(), cp.getY(),z);
			if(isPointOnBinaryChnl( cpSlice, nrgStack, AnySliceCenterOnMask::derivePoint )) {
				return true;
			}
		}
	
		return false;
	}

	private static Point3i derivePoint(Point3d cp) {
		return new Point3i((int) cp.getX(), (int) cp.getY(), (int) cp.getZ());
	}

}
