package ch.ethz.biol.cell.imageprocessing.objmask.provider;

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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramArray;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;


public class ObjMaskCollectionRandomSampler {

	
	public static class ObjMaskSampler {
		
		private ObjectMask objMask;
		private int numPnts;
		private RandomNumberGenerator randomNumberGenerator;
		private Channel chnl;
		
		public ObjMaskSampler(
			ObjectMask objMask,
			Channel chnl,
			RandomNumberGenerator randomNumberGenerator,
			int numPnts
		) {
			super();
			this.objMask = objMask;
			this.chnl = chnl;
			this.numPnts = numPnts;
			this.randomNumberGenerator = randomNumberGenerator;
		}
		
		public int sample() throws OperationFailedException {
			
			BoundingBox bbox = objMask.getBoundingBox(); 
			
			VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte(); 
			
			Extent e = bbox.extent();
			Extent eGlob = vb.extent();
			
			int emergencyBreak = 1000;
			
//			for( int xIndex =0; xIndex<e.getX(); xIndex++) {
//				for( int yIndex =0; yIndex<e.getY(); yIndex++) {
//					for( int zIndex =0; zIndex<e.getZ(); zIndex++) {
						
			
			while(true) {
				int xIndex = (int) (randomNumberGenerator.nextDouble() * e.getX());
				int yIndex = (int) (randomNumberGenerator.nextDouble() * e.getY());
				int zIndex = (int) (randomNumberGenerator.nextDouble() * e.getZ());

				//int yIndex = 21;
				//int zIndex = 5;
				
				int xyIndex = e.offset(xIndex, yIndex);
				
				//System.out.printf("Trying x=%d y=%d z=%d\n",xIndex,yIndex,zIndex);
				
				
				
				//System.out.printf("x=%d y=%d z=%d\n", bbox.getCrnrMin().getX()+xIndex, bbox.getCrnrMin().getY()+yIndex,
				//		zIndex+bbox.getCrnrMin().getZ());
				
				byte buf = objMask.getVoxelBox().getPixelsForPlane(zIndex).buffer().get(xyIndex);
				if (buf==objMask.getBinaryValuesByte().getOnByte()) {
					
					//System.out.println("accepted");
					
					
					int indexXYGlob = eGlob.offset(bbox.getCrnrMin().getX()+xIndex, bbox.getCrnrMin().getY()+yIndex);
					int indexZGlob = zIndex+bbox.getCrnrMin().getZ();	
					
					
					return ByteConverter.unsignedByteToInt(
						vb.getPixelsForPlane(indexZGlob).buffer().get(indexXYGlob)
					);
				}
				
				if (--emergencyBreak==0) {
					break;
				}
			}
//					}
//				}
//			}					
			
			throw new OperationFailedException("Emergency break from loop");
		}

		public int getNumPnts() {
			return numPnts;
		}
	}
	
	
	public static class ObjMaskCollectionSampler {
		
		private List<ObjMaskSampler> list = new ArrayList<>();
		private int cnt = 0;
		private RandomNumberGenerator randomNumberGenerator;
		
		public ObjMaskCollectionSampler(
			ObjectCollection objMaskCollection,
			Channel chnl,
			RandomNumberGenerator randomNumberGenerator
		) {
			this.randomNumberGenerator = randomNumberGenerator;
						
			for( ObjectMask om : objMaskCollection ) {
				int numPixels = om.numVoxelsOn();
				if (numPixels>1) {
					list.add( new ObjMaskSampler(om, chnl, randomNumberGenerator, numPixels) );
					cnt += om.numVoxelsOn();
				}
			}
		}
		
		public int sample() throws OperationFailedException {
			
			int index = (int) (randomNumberGenerator.nextDouble() * cnt);
			
			ObjMaskSampler sampler = findObjMaskSampler(index);
			return sampler.sample();
		}
		
		private ObjMaskSampler findObjMaskSampler( int index ) {
			
			//int ind = 0;
			int c = 0;
			for( ObjMaskSampler oms : list ) {
				c += oms.getNumPnts();
				if (index<c) {
					return oms;
				}
				//ind++;
			}
			
			assert false;
			return null;
		}

		public int size() {
			return list.size();
		}
	}
	
	
	public static Histogram create(
		ObjectCollection omc,
		Channel chnl,
		RandomNumberGenerator randomNumberGenerator,
		int numSamples
	) throws CreateException {
		
		ObjMaskCollectionSampler omcs = new ObjMaskCollectionSampler(
			omc,
			chnl,
			randomNumberGenerator
		);
		
		Histogram out = new HistogramArray(255);
		try {
			if (omcs.size()==0) {
				return out;
			}
			
			for( int i=0; i<numSamples; i++) {
				int val = omcs.sample();
				
				//System.out.printf("%d\n", val);
				
				out.incrVal(val);
			}
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		return out;
	}


}
