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

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactoryByte;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderExtractSliceRange extends ChnlProviderOne {

	// START BEANS
	@BeanField @Positive
	private int sliceStart;
	
	@BeanField @Positive
	private int sliceEnd;
	// END BEANS
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
		
		ChannelFactoryByte factory = new ChannelFactoryByte();
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		
		Extent e = chnl.getDimensions().getExtnt().duplicateChangeZ(
			sliceEnd - sliceStart + 1
		);
		
		Channel chnlOut = factory.createEmptyInitialised( new ImageDim(e, chnl.getDimensions().getRes()) );
		VoxelBox<ByteBuffer> vbOut = chnlOut.getVoxelBox().asByte();
		
		int volumeXY = vb.extent().getVolumeXY();
		for( int z=sliceStart; z<=sliceEnd; z++) {
	
			ByteBuffer bbIn = vb.getPixelsForPlane(z).buffer();
			ByteBuffer bbOut = vbOut.getPixelsForPlane(z-sliceStart).buffer();
			
			for( int i=0; i<volumeXY; i++) {
				bbOut.put(i, bbIn.get(i) );
			}
		}
		
		return chnlOut;
	}

	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
		if (sliceEnd<sliceStart) {
			throw new BeanMisconfiguredException("SliceStart must be less than SliceEnd");
		}
	}

	public int getSliceStart() {
		return sliceStart;
	}

	public void setSliceStart(int sliceStart) {
		this.sliceStart = sliceStart;
	}

	public int getSliceEnd() {
		return sliceEnd;
	}

	public void setSliceEnd(int sliceEnd) {
		this.sliceEnd = sliceEnd;
	}

}
