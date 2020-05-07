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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageRes;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromConnectedComponentsFactory;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;

public class ObjMaskSgmnBinarySgmn extends ObjMaskSgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField
	private int minNumberVoxels = 1;
	// END BEAN PROPERTIES
	
	private ObjMaskCollection createFromBinaryVoxelBox( BinaryVoxelBox<ByteBuffer> bvb, ImageRes res, ObjMask omSrc ) throws SgmnFailedException {
		BinaryChnl bic = new BinaryChnl(
			ChnlFactory.instance().create(bvb.getVoxelBox(), res),
			bvb.getBinaryValues()
		);
		CreateFromConnectedComponentsFactory createObjMasks = new CreateFromConnectedComponentsFactory();
		createObjMasks.setMinNumberVoxels(minNumberVoxels);
		
		ObjMaskCollection objsBinary;
		try {
			objsBinary = createObjMasks.createConnectedComponents(bic );
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
		
		if (omSrc!=null) {
			for (ObjMask om : objsBinary) {
				om.getBoundingBox().getCrnrMin().add(omSrc.getBoundingBox().getCrnrMin());
			}
		}
		
		return objsBinary;
	}
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, SeedCollection seeds)
			throws SgmnFailedException {
	
		BinarySgmnParameters params = new BinarySgmnParameters();
		params.setRes(chnl.getDimensions().getRes());
		
		BinaryVoxelBox<ByteBuffer> bvb = sgmn.sgmn(chnl.getVoxelBox(), params );
		return createFromBinaryVoxelBox(bvb,chnl.getDimensions().getRes(), null);
	}

	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			SeedCollection seeds) throws SgmnFailedException {

		BinarySgmnParameters params = new BinarySgmnParameters();
		params.setRes(chnl.getDimensions().getRes());
		
		BinaryVoxelBox<ByteBuffer> bvb = sgmn.sgmn(chnl.getVoxelBox(), params, objMask );
		return createFromBinaryVoxelBox(bvb,chnl.getDimensions().getRes(), objMask);
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}
	public int getMinNumberVoxels() {
		return minNumberVoxels;
	}

	public void setMinNumberVoxels(int minNumberVoxels) {
		this.minNumberVoxels = minNumberVoxels;
	}


}
