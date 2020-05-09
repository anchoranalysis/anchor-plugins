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
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class ChnlProviderZScore extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private HistogramProvider histogramProvider;
	
	@BeanField
	private boolean alwaysDuplicate = false;
	
	@BeanField
	private double factor = 100.0;		// Multiples
	// END BEAN PROPERTIES
	
	@Override
	public Chnl create() throws CreateException {

		Chnl chnl = chnlProvider.create();
		
		Histogram hist = histogramProvider.create();
		
		VoxelBox<ByteBuffer> out = chnl.getVoxelBox().asByteOrCreateEmpty(alwaysDuplicate);

		try {
			transformBufferToZScore(
				hist.mean(),
				hist.stdDev(),
				chnl,
				out
			);
		} catch (OperationFailedException e) {
			throw new CreateException("An occurred calculating the mean or std-dev of a channel's histogram");
		}
		
		return ChnlFactory.instance().create(out, chnl.getDimensions().getRes());
	}
	
	private void transformBufferToZScore(
		double histMean,
		double histStdDev,
		Chnl chnl,
		VoxelBox<ByteBuffer> out
	) {
		
		// We loop through each item
		Extent e = chnl.getDimensions().getExtnt();
		
		for( int z=0; z<e.getZ(); z++ ) {
			
			VoxelBuffer<?> vbIn = chnl.getVoxelBox().any().getPixelsForPlane(z);
			VoxelBuffer<?> vbOut = out.getPixelsForPlane(z);
			
			for( int offset=0; offset<e.getVolumeXY(); offset++ ) {
				
				int val = vbIn.getInt(offset);
				
				double zScoreDbl = ( ((double) val) - histMean ) / histStdDev;
				
				int valOut = (int) (zScoreDbl*factor);
				
				// We ignore negative zScore
				if (valOut < 0) {
					valOut = 0;
				}
				
				if (valOut > VoxelDataTypeUnsignedByte.MAX_VALUE_INT ) {
					valOut = VoxelDataTypeUnsignedByte.MAX_VALUE_INT;
				}
				
				vbOut.putInt(offset, valOut);
			}
		}
	}
	
	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public boolean isAlwaysDuplicate() {
		return alwaysDuplicate;
	}

	public void setAlwaysDuplicate(boolean alwaysDuplicate) {
		this.alwaysDuplicate = alwaysDuplicate;
	}

}
