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

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.mask.MaskInverter;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;

public class BinaryChnlProviderInvert extends BinaryChnlProviderOne {

	// START BEAN FIELDS
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	
	@BeanField
	private boolean forceChangeBytes = false;
	// END BEAN FIELDS

	@Override
	public Mask createFromChnl( Mask chnl ) throws CreateException {
		
		Optional<Mask> maskChnl = OptionalFactory.create(mask);
		
		if (maskChnl.isPresent()) {
			invertWithMask(chnl, maskChnl.get());
			return chnl;
		}
		
		if (forceChangeBytes) {
			MaskInverter.invertChnl( chnl );
		} else {
			return new Mask(
				chnl.getChannel(),
				chnl.getBinaryValues().createInverted()
			);			
		}
		return chnl;
	}
		
	private void invertWithMask( Mask chnl, Mask mask ) {

		BinaryValuesByte bvb = chnl.getBinaryValues().createByte();
		final byte byteOn = bvb.getOnByte();
		final byte byteOff = bvb.getOffByte();
		
		IterateVoxels.callEachPoint(
			chnl.binaryVoxelBox().getVoxelBox(),				
			mask,
			(Point3i point, ByteBuffer buffer, int offset) -> {
				byte val = buffer.get(offset);
				
				if (val==byteOn) {
					buffer.put(offset,byteOff);
				} else {
					buffer.put(offset,byteOn);
				}
			}
		);
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
