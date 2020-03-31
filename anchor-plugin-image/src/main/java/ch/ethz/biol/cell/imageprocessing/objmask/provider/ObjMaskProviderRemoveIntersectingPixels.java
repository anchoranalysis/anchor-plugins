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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

// Considers all possible pairs of objects in a provider, and removes intersecting pixels
public class ObjMaskProviderRemoveIntersectingPixels extends ObjMaskProviderDimensions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	/**
	 * If TRUE, throws an error if there is a disconnected object after the erosion
	 */
	@BeanField
	private boolean errorDisconnectedObjects = false;
	// END BEAN PROPERTIES

	private void removeIntersectingPixels( ObjMask omWrite, ObjMask omRead, BoundingBox intersection  ) {
		
		BoundingBox bboxRelWrite = new BoundingBox();
		bboxRelWrite.setCrnrMin( intersection.relPosTo( omWrite.getBoundingBox() ));
		bboxRelWrite.setExtnt( intersection.extnt() );
		
		BoundingBox bboxRelRead = new BoundingBox();
		bboxRelRead.setCrnrMin( intersection.relPosTo( omRead.getBoundingBox() ));
		bboxRelRead.setExtnt( intersection.extnt() );
		
		// TODO we can make this more efficient, as we only need to duplicate the intersection area
		//  but for now we don't do anything
		ObjMask omReadDup = omRead.duplicate();
		ObjMask omWriteDup = omWrite.duplicate();
		
		omWrite.getVoxelBox().setPixelsCheckMask(
			bboxRelWrite,
			omReadDup.getVoxelBox(),
			bboxRelRead,
			omWrite.getBinaryValues().getOffInt(),
			omReadDup.getBinaryValuesByte().getOnByte()
		);
	
		omRead.getVoxelBox().setPixelsCheckMask(
			bboxRelRead,
			omWriteDup.getVoxelBox(),
			bboxRelWrite,
			omRead.getBinaryValues().getOffInt(),
			omWriteDup.getBinaryValuesByte().getOnByte()
		);
	}
	
	private void removeIntersectingPixelsIfIntersects( ObjMask omWrite, ObjMask omRead, ImageDim sd ) {

		BoundingBox intersection = omWrite.getBoundingBox().intersectCreateNew(omRead.getBoundingBox(), sd.getExtnt() );
				
		// We check if their bounding boxes intersect
		if(intersection!=null) {
			
			// Let's get a mask for the intersecting pixels
	
			// TODO we can make this more efficient, we only need to duplicate intersection bit
			// We duplicate the originals before everything is changed
			removeIntersectingPixels( omWrite, omRead, intersection );
		}
	}
	
	@Override
	public ObjMaskCollection create() throws CreateException {

		ObjMaskCollection objsCollection = objs.create();
		
		ObjMaskCollection objsDup = objsCollection.duplicate();
		
		ImageDim dims = createDims();
		
		for( int i=0; i<objsCollection.size(); i++) {
			
			ObjMask omWrite = objsDup.get(i);
			
			maybeErrorDisconnectedObjects( omWrite, "before" );
			
			for( int j=0; j<objsCollection.size(); j++) {
				
				ObjMask omRead = objsDup.get(j);
				
				if (i<j) {
					removeIntersectingPixelsIfIntersects( omWrite, omRead, dims );
				}
			}
			
			maybeErrorDisconnectedObjects( omWrite, "after" );
		}
		
		return objsDup;
	}
	
	private void maybeErrorDisconnectedObjects( ObjMask omWrite, String dscr ) throws CreateException {
		if (errorDisconnectedObjects) {
			try {
				if( !omWrite.checkIfConnected() ) {
					throw new CreateException(
						String.format("Obj %s becomes disconnected %s removing intersecting-pixels\n", omWrite, dscr )	
					);
				}
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
		}		
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
	
	public boolean isErrorDisconnectedObjects() {
		return errorDisconnectedObjects;
	}

	public void setErrorDisconnectedObjects(boolean errorDisconnectedObjects) {
		this.errorDisconnectedObjects = errorDisconnectedObjects;
	}


}
