package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public abstract class ChnlProviderConditionallyWriteScalar extends ChnlProviderOneValue {
	
	@Override
	public Channel createFromChnlValue(Channel chnl, double value) throws CreateException {
		processVoxelBox(
			chnl.getVoxelBox().any(),
			value
		);
		return chnl;
	}
	
	/** Whether to overwrite the current voxel-value with the constant? */
	protected abstract boolean shouldOverwriteVoxelWithConstant( int voxel, int constant );
	
	private void processVoxelBox( VoxelBox<?> vb, double value ) {

		int constant = (int) Math.floor(value);
		
		Extent e = vb.extent();
		int volumeXY = e.getVolumeXY();
		for (int z=0; z<e.getZ(); z++) {
			
			VoxelBuffer<?> buf = vb.getPixelsForPlane(z);
			
			for( int i=0; i<volumeXY; i++) {
				
				int voxel = buf.getInt(i); 
				
				if (shouldOverwriteVoxelWithConstant(voxel, constant)) {
					buf.putInt(i,constant);
				}
			}
		}
	}
}
