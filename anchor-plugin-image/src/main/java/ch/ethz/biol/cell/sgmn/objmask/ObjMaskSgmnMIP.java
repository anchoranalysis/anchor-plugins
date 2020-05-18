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
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.ops.ExtendObjsInto3DMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

// TODO needs IInitProposeObjects
public class ObjMaskSgmnMIP extends ObjMaskSgmn {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskSgmn sgmnMIP;
	
	@BeanField
	private BinarySgmn sgmnStack;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection sgmn(Chnl chnl, Optional<SeedCollection> seeds) throws SgmnFailedException {
		
		Chnl max = chnl.maxIntensityProj();
		
		// Collapse seeds in z direction
		seeds.ifPresent(ObjMaskSgmnMIP::flattenSeedsInZ);
		
		ObjMaskCollection objs = sgmnMIP.sgmn(max, seeds);
		
		
		BinarySgmnParameters params = new BinarySgmnParameters(
			chnl.getDimensions().getRes()
		);
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		
		VoxelBox<ByteBuffer> stackBinary = vb.duplicate(); 
		BinaryVoxelBox<ByteBuffer> vbBinary = sgmnStack.sgmn(
			new VoxelBoxWrapper(stackBinary),
			params
		);
		
		return ExtendObjsInto3DMask.extendObjs(objs, vbBinary);
	}
	
	private static SeedCollection flattenSeedsInZ( SeedCollection seeds ) {
		SeedCollection seedsDup = seeds.duplicate();
		seedsDup.flattenZ();
		return seedsDup;
	}

	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			Optional<SeedCollection> seeds) throws SgmnFailedException {
		throw new SgmnFailedException("Unsupported operation");
	}

	public ObjMaskSgmn getSgmnMIP() {
		return sgmnMIP;
	}


	public void setSgmnMIP(ObjMaskSgmn sgmnMIP) {
		this.sgmnMIP = sgmnMIP;
	}


	public BinarySgmn getSgmnStack() {
		return sgmnStack;
	}


	public void setSgmnStack(BinarySgmn sgmnStack) {
		this.sgmnStack = sgmnStack;
	}




}
