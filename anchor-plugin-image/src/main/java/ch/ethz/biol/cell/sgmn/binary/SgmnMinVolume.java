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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

public class SgmnMinVolume extends BinarySgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField @OptionalBean
	private BinarySgmn sgmnToBinarize;
	
	@BeanField @Positive
	private int minNumVoxels;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(
			VoxelBoxWrapper voxelBox, BinarySgmnParameters params, ObjMask objMask) throws SgmnFailedException {
		
		if (minNumVoxels==1) {
			return null;
		}
		
		ObjMask objMaskBuffer = objMask;
		
		if (sgmnToBinarize!=null) {
			BinaryVoxelBox<ByteBuffer> bufferMask = sgmnToBinarize.sgmn( voxelBox, params, objMask );
			
			if (bufferMask==null) {
				return null;
			}
			
			objMaskBuffer = new ObjMask( objMask.getBoundingBox(), bufferMask );
		}

		// Copy objMask into voxelBox
		VoxelBox<ByteBuffer> voxelBoxBox = VoxelBoxFactory.instance().getByte().create( objMaskBuffer.getBoundingBox().extnt() );
		voxelBox.asByte().copyPixelsToCheckMask(
			objMask.getBoundingBox(),
			voxelBoxBox,
			new BoundingBox(objMaskBuffer.getBoundingBox().extnt()),
			objMask.getVoxelBox(),
			objMask.getBinaryValuesByte()
		);
		
		BinaryVoxelBox<ByteBuffer> out = checkVolume( voxelBoxBox, objMaskBuffer.binaryVoxelBox(), params.getRes() );
		
		if (out==null) {
			return null;
		}
		
		return out;
	}

	// Assumes default Binary Values
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params) throws SgmnFailedException {
		
		VoxelBox<ByteBuffer> voxelBoxByte = voxelBox.asByte();
		
		if (sgmnToBinarize!=null) {
			
			VoxelBox<ByteBuffer> orig = voxelBoxByte.duplicate();
			
			BinaryVoxelBox<ByteBuffer> voxelBoxBinary = sgmnToBinarize.sgmn( voxelBox, params ); 
			if (voxelBoxBinary==null) {
				return null;
			}
			
			BoundingBox bboxE = new BoundingBox(voxelBoxByte.extnt());
			
			
			BinaryVoxelBox<ByteBuffer> out = checkVolume( orig, voxelBoxBinary, params.getRes() );
			
			if (out==null) {
				// We copy orig back onto the voxelBox
				orig.copyPixelsTo(bboxE, voxelBoxByte, bboxE);
				return null;
			}
			
			// We copy the out segmentation onto the voxelBox
			out.copyPixelsToCheckMask(bboxE, voxelBoxByte, bboxE, voxelBoxByte, BinaryValuesByte.getDefault() );
			
			return voxelBoxBinary;
			
		} else {
			// Makes no sense to call it this way
			assert false;
			return null;
		}
		
		
	}

	
	private boolean checkVolume( 
			VoxelBox<ByteBuffer> orig,
			BinaryVoxelBox<ByteBuffer> sgmnBuffer,
			BinaryVoxelBox<ByteBuffer> out,
			ImageRes res
		) throws SgmnFailedException {
			
			boolean writtenSomething = false;
			
			ObjMaskCollection omcSecond;
			try {
				omcSecond = SgmnObject.createObjMaskCollectionFromVoxelBox(	sgmnBuffer );
			} catch (CreateException e) {
				throw new SgmnFailedException(e);
			}
			
			for( ObjMask objMaskSecond : omcSecond) {
				
				if (!objMaskSecond.numPixelsLessThan(minNumVoxels)) {
					
					BinarySgmnParameters params = new BinarySgmnParameters();
					params.setRes(res);
					
					BinaryVoxelBox<ByteBuffer> sgmnOut = sgmn.sgmn( new VoxelBoxWrapper(orig), params, objMaskSecond);
					
					if (sgmnOut==null) {
						continue;
					}
					
					ObjMask omNew = new ObjMask(objMaskSecond.getBoundingBox(), sgmnOut);
					out.setPixelsCheckMaskOn( omNew );
					writtenSomething = true;
				}
			}
			return writtenSomething;
		}
				
		private BinaryVoxelBox<ByteBuffer> checkVolume( VoxelBox<ByteBuffer> orig, BinaryVoxelBox<ByteBuffer> sgmnBuffer, ImageRes res ) throws SgmnFailedException {
			
			BinaryVoxelBox<ByteBuffer> out = new BinaryVoxelBoxByte(
				VoxelBoxFactory.instance().getByte().create(
					sgmnBuffer.extnt()
				)
			);

			if(!checkVolume(orig, sgmnBuffer,out, res)) {
				return null;
			}
			
			return out;
		}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}



	public int getMinNumVoxels() {
		return minNumVoxels;
	}

	public void setMinNumVoxels(int minNumVoxels) {
		this.minNumVoxels = minNumVoxels;
	}

	public BinarySgmn getSgmnToBinarize() {
		return sgmnToBinarize;
	}

	public void setSgmnToBinarize(BinarySgmn sgmnToBinarize) {
		this.sgmnToBinarize = sgmnToBinarize;
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}
}