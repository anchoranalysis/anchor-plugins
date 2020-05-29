package ch.ethz.biol.cell.imageprocessing.chnl.provider;

import java.nio.ByteBuffer;

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


import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.CombineTypes;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;

public class ChnlProviderMax extends ChnlProviderTwoVoxelMapping {
	
	public static Chnl createMax( Chnl chnl1, Chnl chnl2 ) throws CreateException {
		
		if (!chnl1.getDimensions().equals(chnl2.getDimensions())) {
			throw new CreateException("Dimensions of channels do not match");
		}
		
		VoxelDataType combinedType = CombineTypes.combineTypes(chnl1.getVoxelDataType(), chnl2.getVoxelDataType());
		Chnl chnlOut = ChnlFactory.instance().createEmptyInitialised( chnl1.getDimensions(), combinedType );
		
		setMaxInOutputVoxelBox(
			chnlOut.getVoxelBox().asByte(),
			chnl1.getVoxelBox().asByte(),
			chnl2.getVoxelBox().asByte()
		);
		
		return chnlOut;
	}

	@Override
	protected void processVoxelBox( VoxelBox<ByteBuffer> vbOut, VoxelBox<ByteBuffer> vbIn1, VoxelBox<ByteBuffer> vbIn2) {
		setMaxInOutputVoxelBox(vbOut, vbIn1, vbIn2);
	}
	
	private static void setMaxInOutputVoxelBox( VoxelBox<ByteBuffer> vbOut, VoxelBox<ByteBuffer> vbIn1, VoxelBox<ByteBuffer> vbIn2) {
		int volumeXY = vbIn1.extent().getVolumeXY();
		for (int z=0; z<vbOut.extent().getZ(); z++) {
			
			VoxelBuffer<?> in1 = vbIn1.getPixelsForPlane(z);
			VoxelBuffer<?> in2 = vbIn2.getPixelsForPlane(z);
			VoxelBuffer<?> out = vbOut.getPixelsForPlane(z);
			
			for (int offset=0; offset<volumeXY; offset++) {
				
				int val1 = in1.getInt(offset);
				int val2 = in2.getInt(offset);
				
				if (val1>val2) {
					out.putInt(offset,val1);
				} else {
					out.putInt(offset,val2);
				}
			}
		}
	}
}
