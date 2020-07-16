package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.geometry.Point3i;

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


import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.math.moment.ImageMoments;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Calculates a matrix of second moments (covariance) of all points in an object-mask.
 * 
 * <p>NOTE, the matrix rows order the eigen-values, so that the first row is highest eigen-value,
 * second row is second-highest etc.</p>
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor @EqualsAndHashCode(callSuper = false)
class CalculateSecondMoments extends FeatureCalculation<ImageMoments,FeatureInputSingleObject> {

	/** Whether to ignore the z-dimension */
	private final boolean suppressZ;

	@Override
	protected ImageMoments execute( FeatureInputSingleObject params ) {
		return new ImageMoments(
			createPointMatrixFromObjectVoxelPositions(params.getObject()),
			suppressZ,
			false
		);
	}
	
	/**
	 * Creates a point-matrix with the distance of each point to the origin of the bounding-box
	 * 
	 * @param object
	 * @return
	 */
	private static DoubleMatrix2D createPointMatrixFromObjectVoxelPositions( ObjectMask object ) {
		
		List<Point3i> listPts = new ArrayList<>();
		
		Extent e = object.getVoxelBox().extent();
		
		
		for (int z=0; z<e.getZ(); z++) {
			ByteBuffer bb = object.getVoxelBox().getPixelsForPlane(z).buffer();
		
			int offset = 0;
			for (int y=0; y<e.getY(); y++) {
				for (int x=0; x<e.getX(); x++) {
					
					if (bb.get(offset++)==object.getBinaryValuesByte().getOnByte()) {
						listPts.add( new Point3i(x,y,z) );
					}
				}
			}
		}
		
		return createPointMatrixInteger( listPts );
	}
	
	private static DoubleMatrix2D createPointMatrixInteger( List<Point3i> points ) {
		DoubleMatrix2D mat = DoubleFactory2D.dense.make( points.size(), 3 );
		for( int i=0; i<points.size(); i++ ) {
			 Point3i point = points.get(i);
			 mat.set(i, 0, point.getX());
			 mat.set(i, 1, point.getY());
			 mat.set(i, 2, point.getZ());
		}
		return mat;
	}
}
