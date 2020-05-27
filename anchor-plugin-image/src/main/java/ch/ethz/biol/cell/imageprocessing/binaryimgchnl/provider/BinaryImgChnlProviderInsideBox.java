package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/** Creates a binary img-chnl where all pixels are 'on' within a certain x, y, z co-ordinate range
 * 
 * This effectively creates a mask that is a box
 * 
 */
public class BinaryImgChnlProviderInsideBox extends BinaryImgChnlProviderDimSource {

	// START BEAN PROPERTIES
	/** Minimum X co-ordinate inclusive */
	@BeanField
	private int minX = 0;
	
	/** Maximum X co-ordinate inclusive */
	@BeanField
	private int maxX = Integer.MAX_VALUE;
	
	/** Minimum Y co-ordinate inclusive */
	@BeanField
	private int minY = 0;
	
	/** Maximum Y co-ordinate inclusive */
	@BeanField
	private int maxY = Integer.MAX_VALUE;
	
	/** Minimum Z co-ordinate inclusive */
	@BeanField
	private int minZ = 0;
	
	/** Maximum Z co-ordinate inclusive */
	@BeanField
	private int maxZ = Integer.MAX_VALUE;
	// END BEAN PROPERTIES
	
	@Override
	protected BinaryChnl createFromSource(ImageDim dimSource) throws CreateException {
		Chnl chnl = ChnlFactory.instance().createEmptyInitialised(
			dimSource,
			VoxelDataTypeUnsignedByte.instance
		);
		
		BoundingBox bbox = createBox(dimSource);
		
		return createBinaryChnl(bbox, chnl);
	}
	
	private static BinaryChnl createBinaryChnl( BoundingBox bbox, Chnl chnl ) {
		BinaryValues bv = BinaryValues.getDefault();
		chnl.getVoxelBox().any().setPixelsTo(bbox, bv.getOnInt() );
		return new BinaryChnl(chnl, bv); 
	}
	
	private BoundingBox createBox( ImageDim sd ) {
		BoundingBox bbox = new BoundingBox(
			new Point3d(minX,minY,minZ),
			new Point3d(maxX,maxY,maxZ)
		);
		
		// Make sure box is inside channel
		return bbox.clipTo( sd.getExtnt() );
	}

	public int getMinX() {
		return minX;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	public int getMinZ() {
		return minZ;
	}

	public void setMinZ(int minZ) {
		this.minZ = minZ;
	}

	public int getMaxZ() {
		return maxZ;
	}

	public void setMaxZ(int maxZ) {
		this.maxZ = maxZ;
	}
}
