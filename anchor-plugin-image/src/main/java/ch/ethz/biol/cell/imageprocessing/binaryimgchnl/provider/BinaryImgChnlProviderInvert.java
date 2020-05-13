package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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

import org.anchoranalysis.bean.ProviderNullableCreator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.BinaryChnlInverter;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class BinaryImgChnlProviderInvert extends BinaryImgChnlProviderOne {

	// START BEAN FIELDS
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	
	@BeanField
	private boolean forceChangeBytes = false;
	// END BEAN FIELDS

	@Override
	public BinaryChnl createFromChnl( BinaryChnl chnl ) throws CreateException {
		
		Optional<BinaryChnl> maskChnl = ProviderNullableCreator.createOptional(mask);
		
		if (maskChnl.isPresent()) {
			invertWithMask(chnl, maskChnl.get());
			return chnl;
		}
		
		if (!forceChangeBytes) {
			BinaryValues bv = chnl.getBinaryValues();
			return new BinaryChnl(chnl.getChnl(), bv.createInverted());
		} else {
			BinaryChnlInverter.invertChnl( chnl );
		}
		return chnl;
	}
		
	private void invertWithMask( BinaryChnl chnl, BinaryChnl mask ) throws CreateException {
		
		BinaryValues bv = chnl.getBinaryValues();
		BinaryValuesByte bvb = bv.createByte();
		BinaryValuesByte bvbMask = mask.getBinaryValues().createByte();
					
		ObjMask maskLocal = mask.createMaskAvoidNew( new BoundingBox( chnl.getDimensions().getExtnt() ) );
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox();
		for (int z=0; z<vb.extnt().getZ(); z++) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			ByteBuffer bbMask = maskLocal.getVoxelBox().getPixelsForPlane(z).buffer();
			
			int offset = 0;
			for (int y=0; y<vb.extnt().getY(); y++) {
				for (int x=0; x<vb.extnt().getX(); x++) {
					
					if( bbMask.get(offset)==bvbMask.getOnByte()) {
					
						byte val = bb.get(offset);
						
						if (val==bvb.getOnByte()) {
							bb.put(offset,bvb.getOffByte());
						} else {
							bb.put(offset,bvb.getOnByte());
						}
					}
					
					offset++;
				}
			}
		}
	}

	public boolean isForceChangeBytes() {
		return forceChangeBytes;
	}

	public void setForceChangeBytes(boolean forceChangeBytes) {
		this.forceChangeBytes = forceChangeBytes;
	}

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}

}
