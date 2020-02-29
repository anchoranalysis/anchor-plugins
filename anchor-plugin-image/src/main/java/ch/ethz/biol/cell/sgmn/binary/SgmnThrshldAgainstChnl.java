package ch.ethz.biol.cell.sgmn.binary;

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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

// Performs a threshold on each pixel, by comparing the pixel value to another channel
//  It sets a pixel as high, if it is greater than or equal to the pixel in the other "Thrshld" channel
public class SgmnThrshldAgainstChnl extends BinarySgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProviderThrshld;
	
	@BeanField
	private boolean clearOutsideMask = true;
	// END BEAN PROPERTIES

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(
			VoxelBoxWrapper voxelBox,
			BinarySgmnParameters params,
			RandomNumberGenerator re) throws SgmnFailedException {

		VoxelBox<?> voxelBoxIn = voxelBox.any();
		VoxelBox<ByteBuffer> voxelBoxOut = createOutputChnl(voxelBox);
		
		Chnl chnlThrshld;
		
		try {
			chnlThrshld = chnlProviderThrshld.create();
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
		
		VoxelBox<?> vbThreshld = chnlThrshld.getVoxelBox().any();
		
		
		if (!vbThreshld.extnt().equals(voxelBoxIn.extnt())) {
			throw new SgmnFailedException("chnlProviderThrshld is of different size to voxelBox");
		}
		
		BinaryValuesByte bvb = BinaryValuesByte.getDefault();
		
		for( int z=0; z<voxelBoxIn.extnt().getZ(); z++ ) {
			
			VoxelBuffer<?> bb = voxelBoxIn.getPixelsForPlane(z);
			VoxelBuffer<ByteBuffer> bbOut = voxelBoxOut.getPixelsForPlane(z);
			VoxelBuffer<?> bbThrshld = vbThreshld.getPixelsForPlane(z);
			
			int offset = 0;
			for( int y=0; y<voxelBoxIn.extnt().getY(); y++) {
				for( int x=0; x<voxelBoxIn.extnt().getX(); x++) {
					
					int val = bb.getInt(offset);
					int valThrshld = bbThrshld.getInt(offset);

					if( val >= valThrshld ) {
						bbOut.buffer().put(offset, bvb.getOnByte());
					} else {
						bbOut.buffer().put(offset, bvb.getOffByte());
					}
					offset++;
				}
			}
		}
		
		return new BinaryVoxelBoxByte( voxelBoxOut, bvb.createInt() );
	}
	
	
	// If the input voxel-box is 8-bit we do it in place
	// Otherwise, we create a new binary voxelbox buffer
	private VoxelBox<ByteBuffer> createOutputChnl( VoxelBoxWrapper voxelBox ) {
		
		if (voxelBox.getVoxelDataType().equals( VoxelDataTypeUnsignedByte.instance )) {
			return voxelBox.asByte();
		} else {
			return VoxelBoxFactory.getByte().create( voxelBox.any().extnt() );
		}
	}
	

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(
			VoxelBoxWrapper voxelBox,
			BinarySgmnParameters params, ObjMask objMask,
			RandomNumberGenerator re) throws SgmnFailedException {

		VoxelBox<?> voxelBoxIn = voxelBox.any();
		VoxelBox<ByteBuffer> voxelBoxOut = createOutputChnl(voxelBox);
		
		Chnl chnlThrshld;
		try {
			chnlThrshld = chnlProviderThrshld.create();
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
		
		VoxelBox<?> vbThrshld = chnlThrshld.getVoxelBox().any();
		
		if (!vbThrshld.extnt().equals(voxelBoxIn.extnt())) {
			throw new SgmnFailedException("chnlProviderThrshld is of different size to voxelBox");
		}
		
		BinaryValuesByte bvb = BinaryValuesByte.getDefault();
		
		Point3i crnrMin = objMask.getBoundingBox().getCrnrMin();
		Point3i crnrMax = objMask.getBoundingBox().calcCrnrMax();
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++ ) {
			
			int relZ = z-crnrMin.getZ();
			
			VoxelBuffer<?> bbIn = voxelBoxIn.getPixelsForPlane(relZ);
			VoxelBuffer<ByteBuffer> bbOut = voxelBoxOut.getPixelsForPlane(relZ);
			VoxelBuffer<?> bbThrshld = vbThrshld.getPixelsForPlane(relZ);
			VoxelBuffer<ByteBuffer> bbMask = objMask.getVoxelBox().getPixelsForPlane(z);
			
			int offsetMask = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++) {
					
					int offset = voxelBoxIn.extnt().offset(x, y);
					
					if (bbMask.buffer().get(offsetMask++)==objMask.getBinaryValuesByte().getOffByte()) {
						
						if (clearOutsideMask) {
							bbOut.buffer().put(offset, bvb.getOffByte());
						}
						
						continue;
					}
										
					int val = bbIn.getInt(offset);
					int valThrshld = bbThrshld.getInt(offset);

					if( val >= valThrshld ) {
						bbOut.buffer().put(offset, bvb.getOnByte());
					} else {
						bbOut.buffer().put(offset, bvb.getOffByte());
					}
				}
			}
		}
		
		return new BinaryVoxelBoxByte( voxelBoxOut, bvb.createInt() );
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}

	public ChnlProvider getChnlProviderThrshld() {
		return chnlProviderThrshld;
	}

	public void setChnlProviderThrshld(ChnlProvider chnlProviderThrshld) {
		this.chnlProviderThrshld = chnlProviderThrshld;
	}

	public boolean isClearOutsideMask() {
		return clearOutsideMask;
	}

	public void setClearOutsideMask(boolean clearOutsideMask) {
		this.clearOutsideMask = clearOutsideMask;
	}

}
