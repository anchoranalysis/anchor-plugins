package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * When the image-gradient is supplied as multiple channels in an NRG stack, this converts it into a list of 
 *  points
 *  
 * A constant is subtracted from the (all positive) image-channels, to make positive or negative values
 * 
 * @author Owen Feehan
 *
 */
public class CalculateGradientFromMultipleChnls extends CachedCalculation<List<Point3d>,FeatureObjMaskParams> {

	private int nrgIndexX;
	
	private int nrgIndexY;
	
	// If -1, then no z-gradient is considered, and all z-values are 0
	private int nrgIndexZ;
	
	private int subtractConstant = 0;
		
	public CalculateGradientFromMultipleChnls(int nrgIndexX, int nrgIndexY,
			int nrgIndexZ, int subtractConstant) {
		super();
		
		this.nrgIndexX = nrgIndexX;
		this.nrgIndexY = nrgIndexY;
		this.nrgIndexZ = nrgIndexZ;
		this.subtractConstant = subtractConstant;
	}
	
	// Always iterates over the list in the same-order
	private void putGradientValue( ObjMask om, List<Point3d> pnts, int axisIndex, Chnl chnl ) {
		
		BinaryVoxelBox<ByteBuffer> bvb = om.binaryVoxelBox();
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		BoundingBox bbox = om.getBoundingBox();


		Extent e = vb.extnt();
		Extent eMask = bbox.extnt();
		
		BinaryValuesByte bvbMask = bvb.getBinaryValues().createByte();
		
		// Tracks where are writing to on the output list.
		int pointIndex = 0;
		
		for( int z=0; z<eMask.getZ(); z++) {
			
			VoxelBuffer<?> bb = vb.getPixelsForPlane(z + bbox.getCrnrMin().getZ() );
			VoxelBuffer<ByteBuffer> bbMask = bvb.getPixelsForPlane(z);
			
			for( int y=0; y<eMask.getY(); y++) {
				for( int x=0; x<eMask.getX(); x++) {
					
					int offsetMask = eMask.offset( x, y);
					
					if (bbMask.buffer().get(offsetMask)==bvbMask.getOnByte()) {
					
						int offset = e.offset(x + bbox.getCrnrMin().getX(), y + bbox.getCrnrMin().getY());
						
						int gradVal = bb.getInt(offset) - subtractConstant;
						
						modifyOrAddPoint( pnts, pointIndex, gradVal, axisIndex );
						pointIndex++;
					}
				}
			}
			
		}
		assert( pnts.size()==pointIndex );
	}
	
	private static void modifyOrAddPoint( List<Point3d> pnts, int pointIndex, int gradVal, int axisIndex ) {
		Point3d out=null;
		if (pointIndex==pnts.size()) {
			out = new Point3d(0,0,0);
			pnts.add(out);
		} else {
			out = pnts.get(pointIndex);
		}
		assert(out!=null);
		
		switch (axisIndex) {
		case 0:
			out.setX(gradVal);
			return;
		case 1:
			out.setY(gradVal);
			return;	
		case 2:
			out.setZ(gradVal);
			return;	
		default:
			assert false;
			return;
		}
	}

	@Override
	protected List<Point3d> execute(FeatureObjMaskParams params)
			throws ExecuteException {

		if (nrgIndexX==-1 || nrgIndexY==-1) {
			throw new ExecuteException( new CreateException("nrgIndexX and nrgIndexY must both be nonZero") );
		}
		
		// create a list of points
		List<Point3d> out = new ArrayList<>();
		
		putGradientValue( params.getObjMask(), out, 0, params.getNrgStack().getNrgStack().getChnl(nrgIndexX) );
		putGradientValue( params.getObjMask(), out, 1, params.getNrgStack().getNrgStack().getChnl(nrgIndexY) );
		
		if (nrgIndexZ!=-1) {
			putGradientValue( params.getObjMask(), out, 2, params.getNrgStack().getNrgStack().getChnl(nrgIndexZ) );
		}
		
		return out;
	}

	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateGradientFromMultipleChnls){
	        final CalculateGradientFromMultipleChnls other = (CalculateGradientFromMultipleChnls) obj;
	        return new EqualsBuilder()
	            .append(nrgIndexX, other.nrgIndexX)
	            .append(nrgIndexY, other.nrgIndexY)
	            .append(nrgIndexZ, other.nrgIndexZ)
	            .append(subtractConstant, other.subtractConstant)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(nrgIndexX).append(nrgIndexY).append(nrgIndexZ).append(subtractConstant).toHashCode();
	}

	@Override
	public CachedCalculation<List<Point3d>,FeatureObjMaskParams> duplicate() {
		return new CalculateGradientFromMultipleChnls( nrgIndexX, nrgIndexY, nrgIndexZ, subtractConstant );
	}
}
