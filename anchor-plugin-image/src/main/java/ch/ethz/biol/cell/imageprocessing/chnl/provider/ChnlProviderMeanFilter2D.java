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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

public class ChnlProviderMeanFilter2D extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField @OptionalBean
	private BinaryImgChnlProvider binaryImgChnlProviderMaskInput;		// A mask on which voxels are considered when calculating the filter
	
	@BeanField @OptionalBean
	private BinaryImgChnlProvider binaryImgChnlProviderMaskOutput;		// A mask on which voxels are outputted with a filter value
	
	@BeanField
	private int radius = 3;	// Should be odd
	// END BEAN PROPERTIES
	
	private static class FilterState {
		private int numPixels = 0;
		private int sumIntensity = 0;
		
		public FilterState() {
			super();
		}
		
		public void addPixel( int val ) {
			sumIntensity += val;
			numPixels++;
		}
		
		public int calcMean() {
			if (numPixels==0) {
				return 0;
			}
			return sumIntensity / numPixels;
		}
	
		public void add( FilterState fs ) {
			this.numPixels = numPixels + fs.numPixels;
			this.sumIntensity = sumIntensity + fs.sumIntensity;
		}
		
		public void subtract( FilterState fs ) {
			this.numPixels = numPixels - fs.numPixels;
			this.sumIntensity = sumIntensity - fs.sumIntensity;
		}
	}
	
	
	public FilterState createAtPosForBox( int x_centre, int y_centre, int z_centre, int sx, int sy, Extent e, ByteBuffer bb ) {
		
		FilterState fs = new FilterState();
		
		int x0 = x_centre - sx;
		int y0 = y_centre - sy;
		int x1 = x_centre + sx;
		int y1 = y_centre + sy;
		
		Point3i pnt = new Point3i();
		pnt.setZ( z_centre );
		
		for( pnt.setY(y0); pnt.getY()<=y1; pnt.incrY() ) {
			for( pnt.setX(x0); pnt.getX()<=x1; pnt.incrX() ) {
				
				if (!e.contains(pnt)) {
					continue;
				}
				
				// 2d offset
				int offset = e.offset(pnt.getX(),pnt.getY());
				int val = ByteConverter.unsignedByteToInt( bb.get(offset) );
				
				fs.addPixel(val);
			}
		}
		
		return fs;
	}
	
	public FilterState createAtPosForBoxMask( int x_centre, int y_centre, int z_centre, int sx, int sy, Extent e, ByteBuffer bb, ByteBuffer bbMask, byte maskOn ) {
		
		FilterState fs = new FilterState();
		
		int x0 = x_centre - sx;
		int y0 = y_centre - sy;
		int x1 = x_centre + sx;
		int y1 = y_centre + sy;
		
		Point3i pnt = new Point3i();
		pnt.setZ(z_centre);
		
		for( pnt.setY(y0); pnt.getY()<=y1; pnt.incrY() ) {
			for( pnt.setX(x0); pnt.getX()<=x1; pnt.incrX() ) {
				
				if (!e.contains(pnt)) {
					continue;
				}
				
				// 2d offset
				int offset = e.offset(pnt.getX(),pnt.getY());
				
				if (bbMask.get(offset)!=maskOn) {
					continue;
				}
				
				int val = ByteConverter.unsignedByteToInt( bb.get(offset) );
				
				fs.addPixel(val);
			}
		}
		
		return fs;
	}
	

	private VoxelBox<ByteBuffer> meanBuffer( VoxelBox<ByteBuffer> vb, BinaryVoxelBox<ByteBuffer> vbMaskInput, BinaryVoxelBox<ByteBuffer> vbMaskOutput ) throws CreateException {
		
		int sx = radius;
		int sy = radius;
		
		Extent e = vb.extnt();
		
		BinaryValuesByte bvbIn = vbMaskInput!=null ? vbMaskInput.getBinaryValues().createByte() : null;
		BinaryValuesByte bvbOut = vbMaskOutput!=null ? vbMaskOutput.getBinaryValues().createByte() : null;
		
		VoxelBox<ByteBuffer> vbOut = VoxelBoxFactory.instance().getByte().create(e); 
		
		for (int z=0; z<e.getZ(); z++) {
		
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			ByteBuffer bbOut = vbOut.getPixelsForPlane(z).buffer();
			ByteBuffer bbMaskInput = vbMaskInput!=null ? vbMaskInput.getPixelsForPlane(z).buffer() : null;
			ByteBuffer bbMaskOutput = vbMaskOutput!=null ? vbMaskOutput.getPixelsForPlane(z).buffer() : null;
			FilterState fs = null;
			
			for( int y=0; y<e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {


					
					if( fs==null) {
						// we create filter state and init
						if (vbMaskInput!=null) {
							fs = createAtPosForBoxMask(x, y, z, sx, sy, e, bb, bbMaskInput, bvbIn.getOnByte());
						} else {
							fs = createAtPosForBox(x, y, z, sx, sy, e, bb);
						}
					} else {
						// we calculate what we need to subtract
						int x0 = x - sx;
						
						if (x0 > 0) {
							// WE SUBTRACT this column
							FilterState fsColumn = vbMaskInput!=null ? createAtPosForBoxMask(x0, y, z, 0, sy, e, bb, bbMaskInput, bvbIn.getOnByte()) : createAtPosForBox(x0, y, z, 0, sy, e, bb);
							fs.subtract( fsColumn );
						}
						
						int x1 = x + sx;
						if (x1 < e.getX()) {
							// WE ADD this column
							FilterState fsColumn =  vbMaskInput!=null ? createAtPosForBoxMask(x1, y, z, 0, sy, e, bb, bbMaskInput, bvbIn.getOnByte()) : createAtPosForBox(x1, y, z, 0, sy, e, bb);
							fs.add( fsColumn );
						}
					}
					
					int offset = e.offset(x,y);
					if (vbMaskOutput!=null) {
						if (bbMaskOutput.get(offset)!=bvbOut.getOnByte()) {
							continue;
						}
					}
					
					bbOut.put(offset, (byte) fs.calcMean() );
				}
				fs=null;
			}
		}	
		return vbOut;
	}
	
	@Override
	public Chnl create() throws CreateException {
		
		
		Chnl chnl = chnlProvider.create(); 
		
		BinaryChnl biInput = null;
		if (binaryImgChnlProviderMaskInput!=null) {
			biInput = binaryImgChnlProviderMaskInput.create();
		}
		
		BinaryChnl biOutput = null;
		if (binaryImgChnlProviderMaskOutput!=null) {
			biOutput = binaryImgChnlProviderMaskOutput.create();
		}
		
		try {
		
			BinaryVoxelBox<ByteBuffer> biVbInput = null;
			if (biInput!=null) {
				VoxelBox<ByteBuffer> mask = biInput.getChnl().getVoxelBox().asByte();
				biVbInput = new BinaryVoxelBoxByte(mask, biInput.getBinaryValues());
			}
			
			BinaryVoxelBox<ByteBuffer> biVbOutput = null;
			if (biOutput!=null) {
				VoxelBox<ByteBuffer> mask = biOutput.getChnl().getVoxelBox().asByte();
				biVbOutput = new BinaryVoxelBoxByte(mask, biOutput.getBinaryValues());
			}
			
			chnl.getVoxelBox().asByte().replaceBy( meanBuffer(chnl.getVoxelBox().asByte(), biVbInput, biVbOutput) );
			
		} catch (IncorrectImageSizeException e ){
			throw new CreateException(e);
		}
		
		return chnl;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}


	public BinaryImgChnlProvider getBinaryImgChnlProviderMaskOutput() {
		return binaryImgChnlProviderMaskOutput;
	}

	public void setBinaryImgChnlProviderMaskOutput(
			BinaryImgChnlProvider binaryImgChnlProviderMaskOutput) {
		this.binaryImgChnlProviderMaskOutput = binaryImgChnlProviderMaskOutput;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderMaskInput() {
		return binaryImgChnlProviderMaskInput;
	}

	public void setBinaryImgChnlProviderMaskInput(
			BinaryImgChnlProvider binaryImgChnlProviderMaskInput) {
		this.binaryImgChnlProviderMaskInput = binaryImgChnlProviderMaskInput;
	}


}
