package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;

// Takes an existing binaryChnl and fits a box around the *On* pixels
//  and makes the box On
public class BinaryImgChnlProviderBox extends BinaryImgChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	// If true, then each z slice is treated seperately
	@BeanField
	private boolean slicesSeperately = false;
	// END BEAN PROPERTIES


	private BoundingBox calcBBoxOfImg( BinaryVoxelBox<ByteBuffer> vb ) throws CreateException {
		
		Point3d negInf = new Point3d( Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY );
		Point3d posInf = new Point3d( Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY );
		
		// We initialize with extreme bounds, so no point is already set
		BoundingBox bbox = new BoundingBox(posInf,negInf);
		
		Extent e = vb.extnt();
		
		BinaryValuesByte bvb = vb.getBinaryValues().createByte();
		
		Point3i pnt = new Point3i(0,0,0);
		for( pnt.setZ(0); pnt.getZ()<e.getZ(); pnt.incrZ() ) {
			
			ByteBuffer buf = vb.getPixelsForPlane(pnt.getZ()).buffer();
			
			for( pnt.setY(0); pnt.getY()<e.getY(); pnt.incrY() ) {
				for( pnt.setX(0); pnt.getX()<e.getX(); pnt.incrX() ) {
				
					int offset = e.offset(pnt.getX(), pnt.getY());
					if (buf.get(offset)==bvb.getOnByte()) {
						bbox.add(pnt);
					}
				}
			}
		}
		
		return bbox;
	}

	@Override
	public BinaryChnl create() throws CreateException {
		
		BinaryChnl bic = binaryImgChnlProvider.create();
		
		if (slicesSeperately) {
			Extent e = bic.getDimensions().getExtnt();
			for( int z=0; z<e.getZ(); z++) {
				BinaryChnl slice = bic.extractSlice(z);
				BoundingBox bbox = calcBBoxOfImg(slice.binaryVoxelBox());
				
				Point3i pnt = bbox.getCrnrMin();
				pnt.setZ(z);
				bbox.setCrnrMin(pnt);
				
				bic.binaryVoxelBox().setPixelsToOn(bbox);
			}
		} else {
			BoundingBox bbox = calcBBoxOfImg(bic.binaryVoxelBox());
			bic.binaryVoxelBox().setPixelsToOn(bbox);
		}
		
		return bic;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public boolean isSlicesSeperately() {
		return slicesSeperately;
	}

	public void setSlicesSeperately(boolean slicesSeperately) {
		this.slicesSeperately = slicesSeperately;
	}


	
}
