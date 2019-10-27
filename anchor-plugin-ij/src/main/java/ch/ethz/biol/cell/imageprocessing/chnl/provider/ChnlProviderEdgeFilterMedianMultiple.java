package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-ij
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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ij.process.ImageProcessor;


// Performs an edge filter after a median filter
///  Does this multiple times for median filter from startRadius to endRadius (increment 2) and averages
public class ChnlProviderEdgeFilterMedianMultiple extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3978226156945187112L;
	
	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private int startRadius;
	
	@BeanField
	private int endRadius;
	// END BEAN

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		
		List<Chnl> allResponses = new ArrayList<>(); 
		
		for( int r=startRadius; r<=endRadius; r+=2) {
			
			Chnl dup = chnl.duplicate();
			VoxelBox<ByteBuffer> vbDup = chnl.getVoxelBox().asByte();
			
			if (r!=0) {
				ChnlProviderMedianFilterIJ2D.median3d(dup, r);
			}
			
			for (int z=0; z<dup.getDimensions().getZ(); z++) {
				ImageProcessor ip = IJWrap.imageProcessorByte(vbDup.getPlaneAccess(),z);
				ip.filter( ImageProcessor.FIND_EDGES );
			}
			
			allResponses.add(dup);
			
			// Special adjustment, so that we jump from r==0 to r=1
			if (r==0) r--;
		}
		
		
		ByteBuffer[] arrByteBuffer = new ByteBuffer[ allResponses.size() ];
		
		Extent e = vb.extnt();
		for( int z=0; z<e.getZ(); z++) {
			
			ByteBuffer bbChnl = vb.getPixelsForPlane(z).buffer();
			
			for( int i=0; i<allResponses.size(); i++) {
				Chnl additional = allResponses.get(i);
				arrByteBuffer[i] = additional.getVoxelBox().asByte().getPixelsForPlane(z).buffer();
			}
			
			
			int offset = 0;
			for( int y=0; y<e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {
					


					double sum = 0;

					for( ByteBuffer bb : arrByteBuffer) {
						sum += ByteConverter.unsignedByteToInt( bb.get(offset) );
					}
					
					
					double result = sum / allResponses.size();
					
					int valOut = (int) Math.round(result);
					
					if (valOut<0) valOut=0;
					if (valOut>255) valOut=255;
					
					bbChnl.put(offset, (byte) valOut );
					
					offset++;
				}
			}
		}
		
		
		
		return chnl;
	}

	public int getEndRadius() {
		return endRadius;
	}

	public void setEndRadius(int endRadius) {
		this.endRadius = endRadius;
	}

	public int getStartRadius() {
		return startRadius;
	}

	public void setStartRadius(int startRadius) {
		this.startRadius = startRadius;
	}

}
