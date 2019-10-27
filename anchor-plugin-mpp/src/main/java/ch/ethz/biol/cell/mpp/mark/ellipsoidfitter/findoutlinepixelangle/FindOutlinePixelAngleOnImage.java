package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.findoutlinepixelangle;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

// Finds a pixel on the outline of an object in a particular direction
public class FindOutlinePixelAngleOnImage extends FindOutlinePixelAngle {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7183706800668575661L;
	
	// START BEANS
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField @Optional
	private UnitValueDistance maxDistance;
	// END BEANS
	
	private BinaryChnl binaryImage = null;
	private Chnl chnl;
	
	public FindOutlinePixelAngleOnImage() {
	}
	
	@Override
	public Point3d pointOnOutline(Point3d centrePoint,
			Orientation orientation) throws OperationFailedException {
		
		// The first time, we establish the binaryImage 
		if (binaryImage==null) {
			try {
				binaryImage = binaryImgChnlProvider.create();
				assert( binaryImage!=null );
				
				chnl = binaryImage.getChnl();
			} catch (CreateException e) {
				throw new OperationFailedException(e);
			}
		}
		
		
		
		assert( binaryImage!=null );
		
		RotationMatrix rotMatrix = orientation.createRotationMatrix();
		
		boolean is3d = rotMatrix.getNumDim() >= 3;
		
		double xMarg = rotMatrix.getMatrix().get(0,0);
		double yMarg = rotMatrix.getMatrix().get(1,0);
		double zMarg = is3d ? rotMatrix.getMatrix().get(2,0) : 0;
		
		BinaryValuesByte bvb = binaryImage.getBinaryValues().createByte();
		
		Point3d pnt = new Point3d(centrePoint.getX(),centrePoint.getY(),centrePoint.getZ());
		while (true) {
			
			pnt.setX( pnt.getX() + xMarg );
			pnt.setY( pnt.getY() + yMarg );
			pnt.setZ( pnt.getZ() + zMarg );

			// We do check
			if (maxDistance!=null) {
				assert( binaryImage != null );
				assert( binaryImage.getDimensions() != null );
				assert( binaryImage.getDimensions().getRes()!=null );
				
				double maxDistRslv = maxDistance.rslv(binaryImage.getDimensions().getRes(), centrePoint, pnt);
				double dist = binaryImage.getDimensions().getRes().distZRel(centrePoint, pnt);
				if (dist>maxDistRslv) {
					return null;
				}
			}
			
			ImageDim sd = binaryImage.getDimensions();
			if (!sd.contains(pnt)) {
				return null;
			}
			
			if ( pntIsOutlineVal(pnt.getX(), pnt.getY(), pnt.getZ(), sd, bvb) ) {
				return pnt;
			}
			
			if ( pntIsOutlineVal(pnt.getX()+1, pnt.getY(), pnt.getZ(), sd, bvb) ) {
				return new Point3d(pnt.getX()+1,pnt.getY(),pnt.getZ());
			}
			
			if ( pntIsOutlineVal(pnt.getX()-1, pnt.getY(), pnt.getZ(), sd, bvb) ) {
				return new Point3d(pnt.getX()-1,pnt.getY(),pnt.getZ());
			}
			
			if ( pntIsOutlineVal(pnt.getX(), pnt.getY()-1, pnt.getZ(), sd, bvb) ) {
				return new Point3d(pnt.getX(),pnt.getY()-1,pnt.getZ());
			}
			
			if ( pntIsOutlineVal(pnt.getX(), pnt.getY()+1, pnt.getZ(), sd, bvb) ) {
				return new Point3d(pnt.getX(),pnt.getY()+1,pnt.getZ());
			}
			
			if (is3d) {
				if ( pntIsOutlineVal(pnt.getX(), pnt.getY(), pnt.getZ()-1, sd, bvb) ) {
					return new Point3d(pnt.getX(),pnt.getY(),pnt.getZ()-1);
				}
				
				if ( pntIsOutlineVal(pnt.getX(), pnt.getY(), pnt.getZ()+1, sd, bvb) ) {
					return new Point3d(pnt.getX(),pnt.getY(),pnt.getZ()+1);
				}
			}
		}

	}
	

	
	private boolean pntIsOutlineVal( double x, double y, double z, ImageDim sd, BinaryValuesByte bvb ) {
		
		if (!sd.contains( new Point3d(x,y,z) )) {
			return false;
		}

		ByteBuffer bb = chnl.getVoxelBox().asByte().getPixelsForPlane( (int) z).buffer();
		return bb.get( sd.offset( (int) x, (int) y ) )== bvb.getOnByte();
	}

	@Override
	public void onInit() throws InitException {
		
		// We shouldn't need to initialise this explicitly
		//binaryImgChnlProvider.initRecursive(pso, getLogger() );
		

		
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public UnitValueDistance getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(UnitValueDistance maxDistance) {
		this.maxDistance = maxDistance;
	}

	



}
