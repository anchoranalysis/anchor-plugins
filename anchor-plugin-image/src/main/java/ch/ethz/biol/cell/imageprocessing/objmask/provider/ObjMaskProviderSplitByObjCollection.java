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


import java.nio.IntBuffer;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxInt;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.box.BoundedVoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

public class ObjMaskProviderSplitByObjCollection extends ObjMaskProviderDimensions {

	private static final CreateFromConnectedComponentsFactory CONNECTED_COMPONENTS_CREATOR
		= new CreateFromConnectedComponentsFactory();
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjectCollectionProvider objsSplitBy;
	// END BEAN PROPERTIES
			
	@Override
	public ObjectCollection createFromObjs(ObjectCollection objsCollection) throws CreateException {
		
		ObjectCollection objsSplitByCollection = objsSplitBy.create();

		ImageDimensions dim = createDim();
		
		try {
			return objsCollection.stream().flatMapWithException(
				OperationFailedException.class,
				obj -> splitObj(
					obj,
					objsSplitByCollection.findObjsWithIntersectingBBox(obj),
					dim
				)
			);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private ObjectCollection splitObj( ObjectMask objToSplit, ObjectCollection objsSplitBy, ImageDimensions dim ) throws OperationFailedException {
		
		// We create a voxel buffer of the same size as objToSplit bounding box, and we write
		//  a number for each obj in ObjsSplitBy
		// Then we find connected components
		
		// Should be set to 0 by default
		BoundedVoxelBox<IntBuffer> boundedVbId = new BoundedVoxelBox<>(
			VoxelBoxFactory.getInt().create(
				objToSplit.getBoundingBox().extent()
			)
		);
		
		// Populate boundedVbId with id values
		int cnt = 1;
		for( ObjectMask omLocal : objsSplitBy ) {
		
			Optional<ObjectMask> intersect = objToSplit.intersect(
				omLocal,
				dim
			);
			
			// If there's no intersection, there's nothing to do
			if (!intersect.isPresent()) {
				continue;
			}
			
			ObjectMask intersectShifted = intersect.get().mapBoundingBox( bbox->
				bbox.shiftBackBy(objToSplit.getBoundingBox().cornerMin())
			); 
			
			// We make the intersection relative to objToSplit
			boundedVbId.getVoxelBox().setPixelsCheckMask(
				intersectShifted,
				cnt++
			);
		}
		
		try {
			// Now we do a flood fill for each number, pretending it's a binary image of 0 and i
			// The code will not change pixels that don't match ON
			return ObjectCollectionFactory.flatMapFromRange(
				1,
				cnt,
				CreateException.class,
				i -> createObjectForIndex(
					i,
					boundedVbId.getVoxelBox(),
					objToSplit.getBoundingBox().cornerMin()
				)
			);
			
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}
	
	/** Creates objects from all connected-components in a buffer with particular voxel values */
	private static ObjectCollection createObjectForIndex( int voxelEqualTo, VoxelBox<IntBuffer> voxels, ReadableTuple3i shiftBy ) throws CreateException {
		BinaryVoxelBox<IntBuffer> binaryVoxels = new BinaryVoxelBoxInt(
			voxels,
			new BinaryValues(0, voxelEqualTo)
		);

		// for every object we add the objToSplit Bounding Box crnr, to restore it to global co-ordinates
		return CONNECTED_COMPONENTS_CREATOR.create(binaryVoxels).shiftBy(shiftBy);
	}

	public ObjectCollectionProvider getObjsSplitBy() {
		return objsSplitBy;
	}

	public void setObjsSplitBy(ObjectCollectionProvider objsSplitBy) {
		this.objsSplitBy = objsSplitBy;
	}

}
