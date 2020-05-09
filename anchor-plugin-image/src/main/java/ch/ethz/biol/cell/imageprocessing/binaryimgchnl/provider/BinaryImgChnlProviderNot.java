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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.extent.Extent;

// Ors the receiveProvider onto the binaryImgChnlProvider
public class BinaryImgChnlProviderNot extends BinaryImgChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider receiveProvider;
	// END BEAN PROPERTIES

	// ASSUMES REGIONS ARE IDENTICAL
	@Override
	public BinaryChnl createFromChnl( BinaryChnl chnlCrnt ) throws CreateException {

		BinaryChnl chnlReceiver = receiveProvider.create();
		
		BinaryValuesByte bvbCrnt = chnlCrnt.getBinaryValues().createByte();
		BinaryValuesByte bvbReceiver = chnlReceiver.getBinaryValues().createByte();
			
		Extent e = chnlCrnt.getDimensions().getExtnt();
			
		byte crntOn = bvbCrnt.getOnByte();
		byte crntOff = bvbCrnt.getOffByte();
		byte receiveOff = bvbReceiver.getOffByte();
		
		// All the on voxels in the receive, are put onto crnt
		for( int z=0; z<e.getZ(); z++ ) {
			
			ByteBuffer bufSrc = chnlCrnt.getVoxelBox().getPixelsForPlane(z).buffer();
			ByteBuffer bufReceive = chnlReceiver.getVoxelBox().getPixelsForPlane(z).buffer();
			
			int offset = 0;
			for( int y=0; y<e.getY(); y++ ) {
				for( int x=0; x<e.getX(); x++ ) {
					
					byte byteSrc = bufSrc.get(offset);
					if (byteSrc==crntOn) {

						byte byteRec = bufReceive.get(offset);
						if (byteRec==receiveOff) {
							bufSrc.put(offset, crntOn);
						} else {
							bufSrc.put(offset, crntOff);
						}

					}
					
					
					offset++;
				}
			}
		}
		
		
		return chnlCrnt;
	}

	public BinaryImgChnlProvider getReceiveProvider() {
		return receiveProvider;
	}

	public void setReceiveProvider(BinaryImgChnlProvider receiveProvider) {
		this.receiveProvider = receiveProvider;
	}

}
