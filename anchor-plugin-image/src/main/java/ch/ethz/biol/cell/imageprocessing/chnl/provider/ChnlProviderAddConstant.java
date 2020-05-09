package ch.ethz.biol.cell.imageprocessing.chnl.provider;

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
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactorySingleType;
import org.anchoranalysis.image.chnl.factory.ChnlFactoryByte;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderAddConstant extends ChnlProvider {

	private static ChnlFactorySingleType factory = new ChnlFactoryByte();
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private int value;
	// END BEAN PROPERTIES

	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		
		if (!chnl.getDimensions().equals(chnl.getDimensions())) {
			throw new CreateException("Dimensions of channels do not match");
		}
		
		Chnl chnlOut = factory.createEmptyInitialised( new ImageDim(chnl.getDimensions()) );
		VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();
		
		for (int z=0; z<chnl.getDimensions().getZ(); z++) {
			
			ByteBuffer in1 = vb.getPixelsForPlane(z).buffer();
			ByteBuffer out = vbOut.getPixelsForPlane(z).buffer();
			
			while (in1.hasRemaining()) {
				
				byte b1 = in1.get();
				
				int mult = ByteConverter.unsignedByteToInt(b1) + value;
				
				if (mult<0) {
					mult=0;
				}
				
				if (mult>255) {
					mult = 255;
				}
				
				out.put( (byte) mult );
			}
		
			assert( !out.hasRemaining() );
		}

		return chnlOut;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}



	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}
	
	public int getValue() {
		return value;
	}



	public void setValue(int value) {
		this.value = value;
	}

}
