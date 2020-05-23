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
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.BinaryChnlInverter;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxelOffsets;

public class BinaryImgChnlProviderInvert extends BinaryImgChnlProviderOne {

	// START BEAN FIELDS
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	
	@BeanField
	private boolean forceChangeBytes = false;
	// END BEAN FIELDS

	@Override
	public BinaryChnl createFromChnl( BinaryChnl chnl ) throws CreateException {
		
		Optional<BinaryChnl> maskChnl = OptionalFactory.create(mask);
		
		if (maskChnl.isPresent()) {
			invertWithMask(chnl, maskChnl.get());
			return chnl;
		}
		
		if (forceChangeBytes) {
			BinaryChnlInverter.invertChnl( chnl );
		} else {
			return new BinaryChnl(
				chnl.getChnl(),
				chnl.getBinaryValues().createInverted()
			);			
		}
		return chnl;
	}
		
	private void invertWithMask( BinaryChnl chnl, BinaryChnl mask ) throws CreateException {
		IterateVoxels.callEachPoint(
			mask,
			new Processer(
				chnl.binaryVoxelBox()
			)
		);
	}
	
	private static final class Processer implements ProcessVoxelOffsets {

		private final BinaryVoxelBox<ByteBuffer> voxelBox;
		private final byte byteOn;
		private final byte byteOff;
		
		private ByteBuffer bbSlice;
		
		public Processer(BinaryVoxelBox<ByteBuffer> voxelBox) {
			super();
			this.voxelBox = voxelBox;
			BinaryValuesByte bvb = voxelBox.getBinaryValues().createByte();
			this.byteOn = bvb.getOnByte();
			this.byteOff = bvb.getOffByte();
		}		
		
		@Override
		public void notifyChangeZ(int z) {
			bbSlice = voxelBox.getPixelsForPlane(z).buffer();
		}
		
		@Override
		public void process(Point3i pnt, int offset3d, int offsetSlice) {
			byte val = bbSlice.get(offsetSlice);
			
			if (val==byteOn) {
				bbSlice.put(offsetSlice,byteOff);
			} else {
				bbSlice.put(offsetSlice,byteOn);
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
