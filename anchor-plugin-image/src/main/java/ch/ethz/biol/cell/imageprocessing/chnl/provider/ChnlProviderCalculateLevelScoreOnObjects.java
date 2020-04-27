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
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderCalculateLevelScoreOnObjects extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private ChnlProvider chnlProviderOutput;
	
	@BeanField
	private ChnlProvider chnlProviderIntensity;
	
	@BeanField
	private ChnlProvider chnlProviderLevel;
	// END BEAN
		
	@Override
	public Chnl create() throws CreateException {

		Chnl chnlIntensity = chnlProviderIntensity.create();
		VoxelBox<ByteBuffer> vbIntensity = chnlIntensity.getVoxelBox().asByte();
		
		Chnl chnlOutput = chnlProviderOutput.create();
		VoxelBox<ByteBuffer> vbOutput = chnlOutput.getVoxelBox().asByte();
		
		Chnl chnlLevel = chnlProviderLevel.create();
		VoxelBox<ByteBuffer> vbLevel = chnlLevel.getVoxelBox().asByte();
		
		ObjMaskCollection objMasks = objs.create();
				
		for( ObjMask om : objMasks ) {
			
			
			
			// Calculate a dist from the quantiles to the level, 
			int level = -1;
			Histogram h;
			int dist = 0;
			
			Extent e = vbOutput.extnt();
			Extent eMask = om.getBoundingBox().extnt();
			BinaryValuesByte bvb = om.getBinaryValuesByte();
			
			for( int z=0; z<eMask.getZ(); z++ ) {
				
				ByteBuffer bufferMask = om.getVoxelBox().getPixelsForPlane(z).buffer();
				
				int z1 = z + om.getBoundingBox().getCrnrMin().getZ();
				
				ByteBuffer buffer = vbIntensity.getPixelsForPlane(z1).buffer();
				ByteBuffer bufferOut = vbOutput.getPixelsForPlane(z1).buffer();
				
				int offset = 0;
				for( int y=0; y<eMask.getY(); y++ ) {
					for( int x=0; x<eMask.getX(); x++ ) {
						
						if (bufferMask.get(offset)==bvb.getOnByte()) {
							
							int x1 = x + om.getBoundingBox().getCrnrMin().getX();
							int y1 = y + om.getBoundingBox().getCrnrMin().getY();
							
							int offsetGlob = e.offset(x1, y1);

							if (level==-1) {
								
								h = HistogramFactoryUtilities.createWithMask(vbIntensity, om);
								
								level = ByteConverter.unsignedByteToInt(
										vbLevel.getPixelsForPlane(z1).buffer().get(offsetGlob)
								);
								
								try {
									int low = level - h.calcMin();
									int high = h.calcMax() - level + 1;
									
									dist = Math.min(low, high);
								} catch (OperationFailedException e1) {
									throw new CreateException("An occurred calculating the min or max of a histogram", e1);
								}
							}
							
							// Calculate the score based upon dist
							int i = ByteConverter.unsignedByteToInt(
								buffer.get(offsetGlob)
							);
							
							double score = calcScore( level, dist, i );
							
							bufferOut.put(offsetGlob, (byte)( score*255) );
							
						}
						offset++;
					}
				}	
			}
			
			
		}
		return chnlOutput;
	}
	
	private static double calcScore( int level, int dist, int intensity ) {
		
		if (intensity>=level) {
			
			if (intensity>=(level+dist)) {
				return 1.0;
			}
			
			double scoreUnscaled = ((double) (intensity-level))/dist;
			return 0.5 + (scoreUnscaled/2);
			
		} else {

			if (intensity<(level-dist)) {
				return 0;
			}
			
			double scoreUnscaled = ((double) (level-intensity))/dist;
			return 0.5 - (scoreUnscaled/2);
		}
	}
	
	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}



	public ChnlProvider getChnlProviderOutput() {
		return chnlProviderOutput;
	}


	public void setChnlProviderOutput(ChnlProvider chnlProviderOutput) {
		this.chnlProviderOutput = chnlProviderOutput;
	}


	public ChnlProvider getChnlProviderIntensity() {
		return chnlProviderIntensity;
	}


	public void setChnlProviderIntensity(ChnlProvider chnlProviderIntensity) {
		this.chnlProviderIntensity = chnlProviderIntensity;
	}



	public ChnlProvider getChnlProviderLevel() {
		return chnlProviderLevel;
	}


	public void setChnlProviderLevel(ChnlProvider chnlProviderLevel) {
		this.chnlProviderLevel = chnlProviderLevel;
	}

	
}
