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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderTwo;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class ChnlProviderSubtract extends ChnlProviderTwo {

	@Override
	protected Chnl process(Chnl chnl1, Chnl chnl2) throws CreateException {
		
		Chnl chnlOut = ChnlFactory.instance().createEmptyInitialised(
			new ImageDim(chnl1.getDimensions()),
			VoxelDataTypeUnsignedByte.instance
		);
		
		processVoxelBox( chnlOut.getVoxelBox().asByte(), chnl1.getVoxelBox().asByte(), chnl2.getVoxelBox().asByte() );
		
		return chnlOut;
	}

	private void processVoxelBox( VoxelBox<ByteBuffer> vbOut, VoxelBox<ByteBuffer> vbIn1, VoxelBox<ByteBuffer> vbIn2) {

		for (int z=0; z<vbOut.extnt().getZ(); z++) {
			
			ByteBuffer in1 = vbIn1.getPixelsForPlane(z).buffer();
			ByteBuffer in2 = vbIn2.getPixelsForPlane(z).buffer();
			ByteBuffer out = vbOut.getPixelsForPlane(z).buffer();
			
			while (in1.hasRemaining()) {
				
				byte b1 = in1.get();
				byte b2 = in2.get();
				
				int diff = ByteConverter.unsignedByteToInt(b1) - ByteConverter.unsignedByteToInt(b2); 
				out.put( (byte) diff );
			}
		
			assert( !in2.hasRemaining() );
			assert( !out.hasRemaining() );
		}
	}
}
