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
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxInt;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.box.BoundedVoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;

public class ObjMaskProviderSplitByObjCollection extends ObjMaskProviderDimensions {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objsSplitBy;
	// END BEAN PROPERTIES
	
	private ObjMaskCollection splitObj( ObjMask objToSplit, ObjMaskCollection objsSplitBy, ImageDim dim ) throws OperationFailedException {
		
		ObjMaskCollection out = new ObjMaskCollection();
		
		// We create a voxel buffer of the same size as objToSplit bounding box, and we write
		//  a number for each obj in ObjsSplitBy
		// Then we find connected components
		
		// Should be set to 0 by default
		VoxelBox<IntBuffer> vbId = VoxelBoxFactory.instance().getInt().create( objToSplit.getBoundingBox().extnt() );
		BoundedVoxelBox<IntBuffer> boundedVbId = new BoundedVoxelBox<>( vbId );
		
		// Populate boundedVbId with id values
		int cnt = 1;
		for( ObjMask omLocal : objsSplitBy ) {
		
			Optional<ObjMask> intersect = objToSplit.intersect(
				omLocal,
				dim
			);
			
			// If there's no intersection, there's nothing to do
			if (!intersect.isPresent()) {
				continue;
			}
			
			intersect.get().getBoundingBox().getCrnrMin().sub(objToSplit.getBoundingBox().getCrnrMin());
			
			// We make the intersection relative to objToSplit
			boundedVbId.getVoxelBox().setPixelsCheckMask( intersect.get(), cnt++);
		}
		
		//TempBoundOutputManager temp = new TempBoundOutputManager();
		//temp.getBoundOutputManager().write("id", new VoxelBoxGenerator(boundedVbId.getVoxelBox()) );
		
		try {
			for( int i=1; i<cnt; i++) {
				
				// Now we do a flood fill for each number, pretending it's a binary image of 0 and i
				// The code will not change pixels that don't match ON
				
				CreateFromConnectedComponentsFactory creator = new CreateFromConnectedComponentsFactory();
				
				BinaryVoxelBox<IntBuffer> bvb = new BinaryVoxelBoxInt(boundedVbId.getVoxelBox(), new BinaryValues(0, i));
				ObjMaskCollection omc = creator.create(bvb);
				
				// for every object we add the objToSplit Bounding Box crnr, to restore it to global co-ordinates
				for( ObjMask om : omc ) {
					om.getBoundingBox().getCrnrMin().add( objToSplit.getBoundingBox().getCrnrMin() );
					out.add(om);
				}
				
				//out.addAll(omc);
			}
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		//out.add( objToSplit );
		return out;
	}
		
	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection objsCollection) throws CreateException {
		
		ObjMaskCollection objsSplitByCollection = objsSplitBy.create();

		ImageDim dims = createDim();
		
		ObjMaskCollection out = new ObjMaskCollection();
		
		for( ObjMask om : objsCollection ) {
			
			// Find all object masks that could potentially intersect with the current oject
			ObjMaskCollection omcIntersecting = objsSplitByCollection.findObjsWithIntersectingBBox(om);
			
			try {
				out.addAll( splitObj(om, omcIntersecting, dims ) );
			} catch (OperationFailedException e) {
				throw new CreateException(e);
			}
		}
		return out;
	}

	public ObjMaskProvider getObjsSplitBy() {
		return objsSplitBy;
	}

	public void setObjsSplitBy(ObjMaskProvider objsSplitBy) {
		this.objsSplitBy = objsSplitBy;
	}

}
