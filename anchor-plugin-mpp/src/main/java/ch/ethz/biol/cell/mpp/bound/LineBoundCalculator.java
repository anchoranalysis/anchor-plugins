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

import org.anchoranalysis.anchor.mpp.bean.bound.BoundCalculator;
import org.anchoranalysis.anchor.mpp.bean.bound.RslvdBound;
import org.anchoranalysis.anchor.mpp.bound.BidirectionalBound;
import org.anchoranalysis.anchor.mpp.proposer.error.ErrorNode;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.math.rotation.RotationMatrix;

public class LineBoundCalculator extends BoundCalculator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6887744677691985554L;

	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider outlineProvider;
	// END BEAN PROPERTIES
		
	//private RslvdBound minMax;
	//private ImgStack stack;

	private double extra = 0;
	
	
	public LineBoundCalculator() {
		super();
	}

	private static RslvdBound createBoundForDirection( double min, double maxDirection ) {
		
		if (maxDirection==-1 || min==-1) {
			return null;
		}
		
		if (min <= maxDirection) {
			return new RslvdBound(min, maxDirection);	
		} else {
			return null;
		}
	}
	
	@Override
	public BidirectionalBound calcBound(Point3d point, RotationMatrix rotMatrix, ErrorNode proposerFailureDescription) {
		
		try {
			//assert(stack!=null);
			
			Chnl outlineChnl = outlineProvider.create().getChnl();
			assert(outlineChnl!=null);
			
			proposerFailureDescription = proposerFailureDescription.add("LineBoundCalculator");
			
			RslvdBound minMax = getSharedObjects().getMarkBounds().calcMinMax( outlineChnl.getDimensions().getRes(), rotMatrix.getNumDim() >= 3 );
					
			int maxPossiblePoint = (int) Math.ceil( minMax.getMax() );
			
			double xMarg = rotMatrix.getMatrix().get(0,0);
			double yMarg = rotMatrix.getMatrix().get(1,0);
			double zMarg = rotMatrix.getNumDim() >= 3 ? rotMatrix.getMatrix().get(2,0) : 0;
			
			// This is 2D Type of code
			double maxReachedFwd = maxReachablePoint( outlineChnl, point, xMarg, yMarg, zMarg, maxPossiblePoint );
			double maxReachedRvrs = maxReachablePoint( outlineChnl, point, -1 * xMarg, -1 * yMarg, -1 * zMarg, maxPossiblePoint );
			
			//System.out.printf("Max-reached-forward=%f   Max-reached-negative=%f\n", maxReachedFwd, maxReachedRvrs);
			
			double min = minMax.getMin();
			
			RslvdBound boundFwd = createBoundForDirection(min, maxReachedFwd );
			RslvdBound boundRvrs = createBoundForDirection(min, maxReachedRvrs );
			
			BidirectionalBound bi = new BidirectionalBound();
			bi.setForward(boundFwd);
			bi.setReverse(boundRvrs);
			
			proposerFailureDescription.addFormatted("forward bound: %s", boundFwd);
			proposerFailureDescription.addFormatted("reverse bound: %s", boundRvrs);
			
			return bi;
		} catch( CreateException e ) {
			proposerFailureDescription.add(e);
			return null;
		} catch (NamedProviderGetException e) {
			proposerFailureDescription.add(e.summarize().toString());
			return null;
		}
	}
	
	private double maxReachablePoint( Chnl voxels, Point3d point, double xMarg, double yMarg, double zMarg, int maxPossiblePoint ) {
		
		VoxelBox<ByteBuffer> vb = voxels.getVoxelBox().asByte();
		
		// This only exists in 2d for now so we can use a slice byteArray
		ByteBuffer arr = null;
		
		int zPrev = 0;
		arr = vb.getPlaneAccess().getPixelsForPlane(zPrev).buffer();
		
		double x = 0;
		double y = 0;
		double z = 0;
		
		for( int i=1; i<maxPossiblePoint; i++) {
			
			x += xMarg;
			y += yMarg;
			z += zMarg;
			
			int xInt = (int)( point.getX() + x );
			int yInt = (int)( point.getY() + y );
			int zInt = (int)( point.getZ() + z );
			
			ImageDim sd = voxels.getDimensions();
			if (xInt >= sd.getX() || xInt < 0 ) {
				return -1; //maxPossiblePoint;
			}
			if (yInt >= sd.getY() || yInt < 0) {
				return -1; //maxPossiblePoint;
			}
			
			if (zInt >= sd.getZ() || zInt < 0) {
				return -1; //maxPossiblePoint;
			}

			
			if (zInt!=zPrev) {
				zPrev = zInt;
				arr = vb.getPlaneAccess().getPixelsForPlane(zPrev).buffer();
			}
			
			int index = sd.offset(xInt, yInt);
			int v = ByteConverter.unsignedByteToInt(arr.get(index));	
				
			if (v>0) {
				// We calculate how far we have travelled in total
				//double sqrt = Math.pow( Math.pow(x, 2.0) + Math.pow(y, 2.0), 0.5 );
				//return sqrt;
				//return extra+i;
				return extra + normZMag(x,y,z, voxels.getDimensions().getRes().getZRelRes() );
			}
		}
		return -1;
	}

	
	private double normZMag( double x, double y, double z, double zMult ) {
		double dx = x;
		double dy = y;
		double dz = z * zMult;
		return Math.sqrt( (dx*dx) + (dy*dy) + (dz*dz) );
	}
	
//	private double euclidDistZMag( Point3d point1, Point3d point2, double zMult ) {
//		double dx = point1.x - point2.x;
//		double dy = point1.y - point2.y;
//		double dz = (point1.z - point2.z) * zMult;
//		return Math.sqrt( (dx*dx) + (dy*dy) + (dz*dz) );
//	}


	@Override
	public boolean paramsEquals(Object other) {
		
		if (!(other instanceof LineBoundCalculator)) {
			return false;
		}
		
		//LineBoundCalculator otherC = (LineBoundCalculator) other;
		
		return true;
	}

	public double getExtra() {
		return extra;
	}

	public void setExtra(double extra) {
		this.extra = extra;
	}

	public BinaryImgChnlProvider getOutlineProvider() {
		return outlineProvider;
	}

	public void setOutlineProvider(BinaryImgChnlProvider outlineProvider) {
		this.outlineProvider = outlineProvider;
	}

}
