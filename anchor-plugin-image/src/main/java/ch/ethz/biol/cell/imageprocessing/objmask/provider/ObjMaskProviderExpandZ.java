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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

// Creates a 3D version of an object by simplying replicated the 2D slice in all z-stacks
public class ObjMaskProviderExpandZ extends ObjMaskProviderDimensions {

	@Override
	public ObjectCollection createFromObjs(ObjectCollection objs) throws CreateException {
		
		ImageDim dim = createDim();
				
		return objs.map( om->
			expandZ(om,dim)
		);
	}
	
	private static ObjectMask expandZ( ObjectMask om, ImageDim dim ) throws CreateException {
		if (om.getBoundingBox().extent().getZ()!=1) {
			throw new CreateException( String.format("Existing object has a z extent of %d. It must be 1", om.getBoundingBox().extent().getZ() ) );
		}
		
		BoundingBox bboxNew = om.getBoundingBox().duplicateChangeExtentZ(dim.getZ());
		
		VoxelBox<ByteBuffer> vbNew = createVoxelBoxOfDuplicatedPlanes(
			om.getVoxelBox().getPixelsForPlane(0),
			bboxNew.extent()
		);
		
		return new ObjectMask(bboxNew, vbNew, om.getBinaryValues());
	}
	
	private static VoxelBox<ByteBuffer> createVoxelBoxOfDuplicatedPlanes( VoxelBuffer<ByteBuffer> planeIn, Extent extent ) {
		VoxelBox<ByteBuffer> vbNew = VoxelBoxFactory.getByte().create(extent);
		for( int z=0; z<extent.getZ(); z++) {
			vbNew.setPixelsForPlane(z, planeIn);
		}
		return vbNew;
	}
}
