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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// TODO consider using a generic histogram-feature and a FeatureCalculation to cache the histogram creation
// TODO consider using some form of histogram thresholder
public class ChnlProviderSuppressAbove extends ChnlProviderOneMask {

	// START BEAN PROPERTIES
	@BeanField
	private double quantile = 0.5;
	// END BEAN PROPERTIES

	@Override
	protected Chnl createFromMaskedChnl(Chnl chnl, BinaryChnl mask) throws CreateException {
		
		Histogram hist = HistogramFactory.create(chnl, mask );
		
		try {
			double intensityThrshldDbl = hist.quantile(quantile);
	
			replacePixelsAbove(
				(int) Math.ceil(intensityThrshldDbl),
				chnl.getVoxelBox().asByte()
			);
		} catch (OperationFailedException e) {
			throw new CreateException("An error occurred computing a quantile", e);
		}
			
		return chnl;
	}
	
	/** Replaces any pixels with value > threshold, with the threshold value */
	private static void replacePixelsAbove( int threshold, VoxelBox<ByteBuffer> vb ) {
		byte meanIntensityByte = (byte) threshold;

		Extent e = vb.extnt();
		
		for( int z=0; z<e.getZ(); z++) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			int offset = 0;
			for( int y=0; y<e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {
			
					int val = ByteConverter.unsignedByteToInt(bb.get(offset));
					if (val>threshold) {
						bb.put(offset, meanIntensityByte );
					}
					
					offset++;
				}
			}
		}
	}

	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}
}
