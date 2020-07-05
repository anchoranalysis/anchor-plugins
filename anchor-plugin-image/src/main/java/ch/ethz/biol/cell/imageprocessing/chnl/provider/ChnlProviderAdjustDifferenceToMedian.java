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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// Corrects a channel in the following way
//  For each object:
//		1. Identify the median value from channelLookup
//		2. Calculate the difference of each pixel value in channelLookup to Value 1.
//		3. Adjust each pixel value by Value 2.
public class ChnlProviderAdjustDifferenceToMedian extends ChnlProviderOneObjsSource {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlLookup;
	// END BEAN PROPERTIES
	
	@Override
	protected Channel createFromChnl(Channel chnl, ObjectCollection objsSource) throws CreateException {
	
		Channel lookup = DimChecker.createSameSize(chnlLookup, "chnlLookup", chnl);
		
		try {
			for( ObjectMask om : objsSource ) {
				Histogram h = HistogramFactory.create(
					lookup.getVoxelBox(),
					Optional.of(om)
				);
				int objMedian = (int) Math.round(h.mean());
				adjustObj(om, chnl, lookup, objMedian );
	
			}
			
			return chnl;
			
		} catch (OperationFailedException e) {
			throw new CreateException("An error occurred calculating the mean", e);
		}
	}
	
	private void adjustObj( ObjectMask om, Channel chnl, Channel chnlLookup, int objMedian ) {
		
		ReadableTuple3i crnrMin = om.getBoundingBox().cornerMin();
		ReadableTuple3i crnrMax = om.getBoundingBox().calcCornerMax();
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		VoxelBox<ByteBuffer> vbLookup = chnlLookup.getVoxelBox().asByte();
		
		for( int z=crnrMin.getZ(); z<=crnrMax.getZ(); z++ ) {
			
			ByteBuffer bbChnl = vb.getPixelsForPlane(z).buffer();
			ByteBuffer bbChnlLookup = vbLookup.getPixelsForPlane(z).buffer();
			ByteBuffer bbMask = om.getVoxelBox().getPixelsForPlane(z-crnrMin.getZ()).buffer();
			
			int maskOffset = 0;
			for( int y=crnrMin.getY(); y<=crnrMax.getY(); y++ ) {
				for( int x=crnrMin.getX(); x<=crnrMax.getX(); x++ ) {
					
					if( bbMask.get(maskOffset++)==om.getBinaryValuesByte().getOnByte()) {
						
						int offset = vb.extent().offset(x, y);
						
						int lookupVal = ByteConverter.unsignedByteToInt( bbChnlLookup.get(offset) );
						int adj = (objMedian - lookupVal);
						
						int crntVal = ByteConverter.unsignedByteToInt( bbChnl.get(offset) );
						int valNew = crntVal - adj;
						
						if (valNew<0) valNew = 0;
						if (valNew>255) valNew = 255;
						
						bbChnl.put( offset, (byte) valNew);
					}
					
				}
			}
		}
	}

	public ChnlProvider getChnlLookup() {
		return chnlLookup;
	}

	public void setChnlLookup(ChnlProvider chnlLookup) {
		this.chnlLookup = chnlLookup;
	}

}
