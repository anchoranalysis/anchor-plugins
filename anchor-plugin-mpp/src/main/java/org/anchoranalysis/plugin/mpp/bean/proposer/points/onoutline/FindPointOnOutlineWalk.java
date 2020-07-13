package org.anchoranalysis.plugin.mpp.bean.proposer.points.onoutline;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation;
import org.anchoranalysis.math.rotation.RotationMatrix;

/**
 * Walks in a particular direction until the outline is found.
 *
 **/
public class FindPointOnOutlineWalk extends FindPointOnOutline {

	// START BEANS
	@BeanField
	private BinaryChnlProvider binaryChnl;
	
	@BeanField @OptionalBean
	private UnitValueDistance maxDistance;
	// END BEANS
	
	private BinaryChnl binaryImage = null;
	private Channel chnl;
	
	@Override
	public Optional<Point3i> pointOnOutline(Point3d centrePoint, Orientation orientation) throws OperationFailedException {
		
		// The first time, we establish the binaryImage 
		if (binaryImage==null) {
			try {
				binaryImage = binaryChnl.create();
				assert( binaryImage!=null );
				
				chnl = binaryImage.getChannel();
			} catch (CreateException e) {
				throw new OperationFailedException(e);
			}
		}
		
		
		
		assert( binaryImage!=null );
		
		RotationMatrix rotMatrix = orientation.createRotationMatrix();
		
		boolean is3d = rotMatrix.getNumDim() >= 3;
		
		Point3d marg = new Point3d(
			rotMatrix.getMatrix().get(0,0),
			rotMatrix.getMatrix().get(1,0),
			is3d ? rotMatrix.getMatrix().get(2,0) : 0
		);
		
		BinaryValuesByte bvb = binaryImage.getBinaryValues().createByte();
		
		Point3d pntDouble = new Point3d(centrePoint);
		while (true) {
			
			pntDouble.increment(marg);
			
			Point3i pnt = PointConverter.intFromDouble(pntDouble);

			// We do check
			if (maxDistance!=null) {
				assert( binaryImage != null );
				assert( binaryImage.getDimensions() != null );
				assert( binaryImage.getDimensions().getRes()!=null );
				
				double maxDistRslv = maxDistance.rslv(
					Optional.of(
						binaryImage.getDimensions().getRes()
					),
					centrePoint,
					pntDouble
				);
				double dist = binaryImage.getDimensions().getRes().distZRel(centrePoint, pntDouble);
				if (dist>maxDistRslv) {
					return Optional.empty();
				}
			}
			
			ImageDimensions sd = binaryImage.getDimensions();
			if (!sd.contains(pnt)) {
				return Optional.empty();
			}
			
			// TODO replace what follows with a call to IterateVoxels.callEachPointInNghb 
			
			if ( pntIsOutlineVal(pnt, sd, bvb) ) {
				return Optional.of(pnt);
			}
			
			pnt.incrementX();
			
			if ( pntIsOutlineVal(pnt, sd, bvb) ) {
				return Optional.of(pnt);
			}
			
			pnt.decrementX(2);
			
			if ( pntIsOutlineVal(pnt, sd, bvb) ) {
				return Optional.of(pnt);
			}
			
			pnt.incrementX();
			pnt.decrementY();
			
			if ( pntIsOutlineVal(pnt, sd, bvb) ) {
				return Optional.of(pnt);
			}
			
			pnt.incrementY(2);
			
			if ( pntIsOutlineVal(pnt, sd, bvb) ) {
				return Optional.of(pnt);
			}
			
			pnt.decrementY();
			
			if (is3d) {
				pnt.decrementZ();
				if ( pntIsOutlineVal(pnt, sd, bvb) ) {
					return Optional.of(pnt);
				}
				
				pnt.incrementZ(2);
				if ( pntIsOutlineVal(pnt, sd, bvb) ) {
					return Optional.of(pnt);
				}
			}
		}
	}
		
	private boolean pntIsOutlineVal( Point3i pnt, ImageDimensions sd, BinaryValuesByte bvb ) {
		
		if (!sd.contains(pnt)) {
			return false;
		}

		ByteBuffer bb = chnl.getVoxelBox().asByte().getPixelsForPlane(
			pnt.getZ()
		).buffer();
		return bb.get(sd.offsetSlice(pnt))== bvb.getOnByte();
	}

	public UnitValueDistance getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(UnitValueDistance maxDistance) {
		this.maxDistance = maxDistance;
	}

	public BinaryChnlProvider getBinaryChnl() {
		return binaryChnl;
	}

	public void setBinaryChnl(BinaryChnlProvider binaryChnl) {
		this.binaryChnl = binaryChnl;
	}
}
