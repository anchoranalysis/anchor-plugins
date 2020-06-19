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
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmnOne;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objectmask.ops.ExtendObjsInto3DMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
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
public class ObjMaskSgmnMIP extends ObjMaskSgmnOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmnStack;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection sgmn(Channel chnl, Optional<ObjectMask> objMask, Optional<SeedCollection> seeds, ObjMaskSgmn sgmn) throws SgmnFailedException {
		
		if (objMask.isPresent()) {
			throw new SgmnFailedException("An object-mask is not supported for this operation");
		}
		
		Channel max = chnl.maxIntensityProjection();
		
		// Collapse seeds in z direction
		seeds.ifPresent(ObjMaskSgmnMIP::flattenSeedsInZ);
		
		ObjectCollection objs = sgmn.sgmn(max, Optional.empty(), seeds);
		
		if (isAny3d(objs)) {
			throw new SgmnFailedException("A 3D object was returned from the initial segmentation. This must return only 2D objects");
		}
		
		return ExtendObjsInto3DMask.extendObjs(
			objs,
			binarySgmn(chnl)
		);
	}
	
	private boolean isAny3d(ObjectCollection objs) {
		return objs.anyMatch(om->
			om.getVoxelBox().extent().getZ() >1
		);
	}
	
	private BinaryVoxelBox<ByteBuffer> binarySgmn(Channel chnl) throws SgmnFailedException {
		BinarySgmnParameters params = new BinarySgmnParameters(
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

	public BinarySgmn getSgmnStack() {
		return sgmnStack;
	}

	public void setSgmnStack(BinarySgmn sgmnStack) {
		this.sgmnStack = sgmnStack;
	}
}
