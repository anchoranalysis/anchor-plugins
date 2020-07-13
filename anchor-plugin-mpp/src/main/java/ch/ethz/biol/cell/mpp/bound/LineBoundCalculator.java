package ch.ethz.biol.cell.mpp.bound;

/*
 * #%L
 * anchor-plugin-mpp
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
import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.math.rotation.RotationMatrix;

import lombok.Getter;
import lombok.Setter;

public class LineBoundCalculator extends BoundCalculator {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private BinaryChnlProvider outlineProvider;
	
	@BeanField @Getter @Setter
	private double extra = 0;
	// END BEAN PROPERTIES
	
	@Override
	public BidirectionalBound calcBound(Point3d point, RotationMatrix rotMatrix) throws OperationFailedException {
		
		try {
			Channel outlineChnl = outlineProvider.create().getChannel();
			assert(outlineChnl!=null);
			
			RslvdBound minMax = getInitializationParameters().getMarkBounds().calcMinMax(
				outlineChnl.getDimensions().getRes(),
				rotMatrix.getNumDim() >= 3
			);
					
			int maxPossiblePoint = (int) Math.ceil( minMax.getMax() );
			
			Point3d marg = new Point3d(
				rotMatrix.getMatrix().get(0,0),
				rotMatrix.getMatrix().get(1,0),
				rotMatrix.getNumDim() >= 3 ? rotMatrix.getMatrix().get(2,0) : 0
			);
			Point3d margInverse = Point3d.immutableScale(marg, -1);
			
			// This is 2D Type of code
			double maxReachedFwd = maxReachablePoint(outlineChnl, point, marg, maxPossiblePoint );
			double maxReachedRvrs = maxReachablePoint( outlineChnl, point, margInverse, maxPossiblePoint );
			
			double min = minMax.getMin();
			
			return new BidirectionalBound(
				createBoundForDirection(min, maxReachedFwd),
				createBoundForDirection(min, maxReachedRvrs)
			);
			
		} catch( CreateException e ) {
			throw new OperationFailedException(e);
		} catch (NamedProviderGetException e) {
			throw new OperationFailedException(e.summarize());
		}
	}

	private static Optional<RslvdBound> createBoundForDirection( double min, double maxDirection ) {
		
		if (maxDirection==-1 || min==-1) {
			return Optional.empty();
		}
		
		if (min <= maxDirection) {
			return Optional.of(
				new RslvdBound(min, maxDirection)
			);
		} else {
			return Optional.empty();
		}
	}
	
	private double maxReachablePoint( Channel voxels, Point3d point, Point3d marg, int maxPossiblePoint ) {
		
		VoxelBox<ByteBuffer> vb = voxels.getVoxelBox().asByte();
		
		// This only exists in 2d for now so we can use a slice byteArray
		ByteBuffer arr = null;
		
		int zPrev = 0;
		arr = vb.getPlaneAccess().getPixelsForPlane(zPrev).buffer();

		Point3d runningDbl = new Point3d();
		
		for( int i=1; i<maxPossiblePoint; i++) {
			runningDbl.add(marg);
			
			Point3i runningInt = PointConverter.intFromDouble( 
				Point3d.immutableAdd(
					point,
					runningDbl
				)
			);
			
			ImageDimensions sd = voxels.getDimensions();
			if (sd.contains(runningInt)) {
				return -1;
			}
			
			if (runningInt.getZ()!=zPrev) {
				zPrev = runningInt.getZ();
				arr = vb.getPlaneAccess().getPixelsForPlane(zPrev).buffer();
			}
			
			int index = sd.offsetSlice(runningInt);
			int v = ByteConverter.unsignedByteToInt(arr.get(index));	
				
			if (v>0) {
				// We calculate how far we have travelled in total
				return extra + normZMag(runningDbl, voxels.getDimensions().getRes().getZRelRes() );
			}
		}
		return -1;
	}
	
	private double normZMag( Point3d pnt, double zMult ) {
		double dx = pnt.getX();
		double dy = pnt.getY();
		double dz = pnt.getZ() * zMult;
		return Math.sqrt( (dx*dx) + (dy*dy) + (dz*dz) );
	}
}
