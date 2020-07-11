package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

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
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

import lombok.Getter;
import lombok.Setter;

// Considers all possible pairs of objects in a provider, and removes intersecting pixels
public class ObjMaskProviderRemoveIntersectingVoxels extends ObjMaskProviderDimensions {

	// START BEAN PROPERTIES
	/**
	 * If TRUE, throws an error if there is a disconnected object after the erosion
	 */
	@BeanField @Getter @Setter
	private boolean errorDisconnectedObjects = false;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection createFromObjs( ObjectCollection objsCollection) throws CreateException {
		
		ObjectCollection objsDup = objsCollection.duplicate();
		
		ImageDimensions dims = createDim();
		
		for( int i=0; i<objsCollection.size(); i++) {
			
			ObjectMask omWrite = objsDup.get(i);
			
			maybeErrorDisconnectedObjects( omWrite, "before" );
			
			for( int j=0; j<objsCollection.size(); j++) {
				
				ObjectMask omRead = objsDup.get(j);
				
				if (i<j) {
					removeIntersectingVoxelsIfIntersects( omWrite, omRead, dims );
				}
			}
			
			maybeErrorDisconnectedObjects( omWrite, "after" );
		}
		
		return objsDup;
	}
	
	private void removeIntersectingVoxels( ObjectMask omWrite, ObjectMask omRead, BoundingBox intersection  ) {
		
		BoundingBox bboxRelWrite = new BoundingBox(
			intersection.relPosTo( omWrite.getBoundingBox() ),
			intersection.extent()
		);
		
		BoundingBox bboxRelRead = new BoundingBox(
			intersection.relPosTo( omRead.getBoundingBox() ),
			intersection.extent()
		);
		
		// TODO we can make this more efficient, as we only need to duplicate the intersection area
		ObjectMask omReadDup = omRead.duplicate();
		ObjectMask omWriteDup = omWrite.duplicate();
		
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
	
	private void removeIntersectingVoxelsIfIntersects( ObjectMask omWrite, ObjectMask omRead, ImageDimensions sd ) {

		Optional<BoundingBox> intersection = omWrite.getBoundingBox().intersection().withInside(omRead.getBoundingBox(), sd.getExtent() );
				
		// We check if their bounding boxes intersect
		if(intersection.isPresent()) {
			
			// Let's get a mask for the intersecting pixels
	
			// TODO we can make this more efficient, we only need to duplicate intersection bit
			// We duplicate the originals before everything is changed
			removeIntersectingVoxels( omWrite, omRead, intersection.get() );
		}
	}
		
	private void maybeErrorDisconnectedObjects( ObjectMask omWrite, String dscr ) throws CreateException {
		if (errorDisconnectedObjects) {
			try {
				if( !omWrite.checkIfConnected() ) {
					throw new CreateException(
						String.format("Obj %s becomes disconnected %s removing intersecting-pixels%n", omWrite, dscr )	
					);
				}
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
		}		
	}
}
