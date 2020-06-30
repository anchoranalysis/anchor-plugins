package org.anchoranalysis.plugin.image.feature.object.calculation.single.moment;

/*
 * #%L
 * anchor-points
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


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;


/**
 * Calculates moments of all point-locations in an ObjMask
 * 
 * By moments, we mean first (mean) and the eigen-values of the second moment (covariance)
 * 
 */
class MomentsFromObjMask {

	private MomentsFromObjMask() {
		
	}

	/**
	 * Calculates moments of all point-locations in an ObjMask
	 * 
	 * NOTE, eigenValues are in ascending numerical order
	 * 
	 * @param om object-mask
	 * @param suppressZ whether we ignore the z-dimension
	 * @return a matrix containing the second-moments
	 */
	public static MomentsFromPointsCalculator apply( ObjectMask om, boolean suppressZ  ) {
		return new MomentsFromPointsCalculator(
			createPointMatrixFromObjMaskPixelPositions(om),
			suppressZ,
			false
		);
	}

	/**
	 * Creates a point-matrix with the distance of each point to the origin of the bounding-box
	 * 
	 * @param om
	 * @return
	 */
	private static DoubleMatrix2D createPointMatrixFromObjMaskPixelPositions( ObjectMask om ) {
		
		List<Point3i> listPts = new ArrayList<>();
		
		Extent e = om.getVoxelBox().extent();
		
		
		for (int z=0; z<e.getZ(); z++) {
			ByteBuffer bb = om.getVoxelBox().getPixelsForPlane(z).buffer();
		
			int offset = 0;
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					if (bb.get(offset++)==om.getBinaryValuesByte().getOnByte()) {
						listPts.add( new Point3i(x,y,z) );
					}
				}
			}
		}
		
		return createPointMatrixInteger( listPts );
	}
	
	private static DoubleMatrix2D createPointMatrixInteger( List<Point3i> pnts ) {
		DoubleMatrix2D mat = DoubleFactory2D.dense.make( pnts.size(), 3 );
		for( int i=0; i<pnts.size(); i++ ) {
			 Point3i p = pnts.get(i);
			 mat.set(i, 0, p.getX());
			 mat.set(i, 1, p.getY());
			 mat.set(i, 2, p.getZ());
		}
		return mat;
	}

}
