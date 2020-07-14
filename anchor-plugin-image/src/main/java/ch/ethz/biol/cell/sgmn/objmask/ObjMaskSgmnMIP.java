package ch.ethz.biol.cell.sgmn.objmask;

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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.nonbean.error.SgmnFailedException;
import org.anchoranalysis.image.bean.nonbean.parameters.BinarySegmentationParameters;
import org.anchoranalysis.image.bean.segmentation.binary.BinarySegmentation;
import org.anchoranalysis.image.bean.segmentation.object.ObjectSegmentation;
import org.anchoranalysis.image.bean.segmentation.object.ObjectSegmentationOne;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.ops.ExtendObjsInto3DMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

/**
 * Perform a segmentation in a MIP instead of z-stacks.
 * 
 * <p>The incoming segmentation should return 2D objects</p>.
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskSgmnMIP extends ObjectSegmentationOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySegmentation sgmnStack;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection sgmn(Channel chnl, Optional<ObjectMask> object, Optional<SeedCollection> seeds, ObjectSegmentation sgmn) throws SgmnFailedException {
		
		if (object.isPresent()) {
			throw new SgmnFailedException("An object-mask is not supported for this operation");
		}
		
		Channel max = chnl.maxIntensityProjection();
		
		// Collapse seeds in z direction
		seeds.ifPresent(ObjMaskSgmnMIP::flattenSeedsInZ);
		
		ObjectCollection objects = sgmn.sgmn(max, Optional.empty(), seeds);
		
		if (isAny3d(objects)) {
			throw new SgmnFailedException("A 3D object was returned from the initial segmentation. This must return only 2D objects");
		}
		
		return ExtendObjsInto3DMask.extendObjs(
			objects,
			binarySgmn(chnl)
		);
	}
	
	private boolean isAny3d(ObjectCollection objects) {
		return objects.stream().anyMatch(om->
			om.getVoxelBox().extent().getZ() >1
		);
	}
	
	private BinaryVoxelBox<ByteBuffer> binarySgmn(Channel chnl) throws SgmnFailedException {
		BinarySegmentationParameters params = new BinarySegmentationParameters(
			chnl.getDimensions().getRes()
		);
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		
		VoxelBox<ByteBuffer> stackBinary = vb.duplicate(); 
		return sgmnStack.sgmn(
			new VoxelBoxWrapper(stackBinary),
			params,
			Optional.empty()
		);
	}
	
	private static SeedCollection flattenSeedsInZ( SeedCollection seeds ) {
		SeedCollection seedsDup = seeds.duplicate();
		seedsDup.flattenZ();
		return seedsDup;
	}

	public BinarySegmentation getSgmnStack() {
		return sgmnStack;
	}

	public void setSgmnStack(BinarySegmentation sgmnStack) {
		this.sgmnStack = sgmnStack;
	}
}
