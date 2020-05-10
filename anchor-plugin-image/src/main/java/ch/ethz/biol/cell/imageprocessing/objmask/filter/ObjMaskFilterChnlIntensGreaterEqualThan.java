package ch.ethz.biol.cell.imageprocessing.objmask.filter;

/*
 * #%L
 * anchor-plugin-image
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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.orientation.DirectionVector;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

// Only allows an ObjMask through if at least one of its voxels has an intensity value
//   greater than or equal to a threshold value from the ChnlProvider
public class ObjMaskFilterChnlIntensGreaterEqualThan extends ObjMaskFilterByObject {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	// The threshold we use, the distance is always calculated in the direction of the XY plane.
	@BeanField
	private UnitValueDistance threshold;
	// END BEAN PROPERTIES
	
	private VoxelBox<?> vb;

	@Override
	protected void start() throws OperationFailedException {
		
		Chnl chnlSingleRegion;
		try {
			chnlSingleRegion = chnlProvider.create();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		assert(chnlSingleRegion!=null);
		vb = chnlSingleRegion.getVoxelBox().any();
	}

	@Override
	protected boolean match(ObjMask om, Optional<ImageDim> dim) throws OperationFailedException {
		
		if (!dim.isPresent()) {
			throw new OperationFailedException("Image-dimensions are required for this operation");
		}
		
		int thresholdRslv = threshold(dim);
		
		for( int z=0; z<om.getBoundingBox().extnt().getZ(); z++) {
			
			ByteBuffer bb = om.getVoxelBox().getPixelsForPlane(z).buffer();
			
			int z1 = z + om.getBoundingBox().getCrnrMin().getZ();
			VoxelBuffer<?> bbChnl = vb.getPixelsForPlane(z1);
			
			for( int y=0; y<om.getBoundingBox().extnt().getY(); y++) {
				for( int x=0; x<om.getBoundingBox().extnt().getX(); x++) {
			
					int offset = om.getBoundingBox().extnt().offset(x, y);
					if( bb.get(offset)==om.getBinaryValuesByte().getOnByte() ) {
						
						int y1 = y + om.getBoundingBox().getCrnrMin().getY();
						int x1 = x + om.getBoundingBox().getCrnrMin().getX();
						
						int offsetGlobal = vb.extnt().offset(x1,y1);
						
						// Now we get a value from the vb
						int val = bbChnl.getInt(offsetGlobal);
						if (val>=thresholdRslv) {
							return true;
						}
					}
					
					
				}
			}
		}
		
		return false;
	}

	private int threshold(Optional<ImageDim> dim) throws OperationFailedException {
		return (int) Math.ceil(
			threshold.rslv(
				dim.map(ImageDim::getRes),
				new DirectionVector(1.0, 0, 0)
			)
		);
	}
	
	@Override
	protected void end() throws OperationFailedException {
		vb = null;
	}


	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}


	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}


	public UnitValueDistance getThreshold() {
		return threshold;
	}


	public void setThreshold(UnitValueDistance threshold) {
		this.threshold = threshold;
	}




}
