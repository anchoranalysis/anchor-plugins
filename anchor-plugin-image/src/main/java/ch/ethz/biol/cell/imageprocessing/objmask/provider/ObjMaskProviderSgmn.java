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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.factory.CreateFromEntireChnlFactory;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;

public class ObjMaskProviderSgmn extends ObjMaskProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	
	@BeanField
	private ObjMaskSgmn sgmn;
	
	@BeanField @OptionalBean
	private ObjMaskProvider objsSeeds;
	// END BEAN PROPERTIES

	@Override
	protected ObjMaskCollection createFromChnl(Chnl chnlSrc) throws CreateException {

		if (mask!=null) {
			return createWithMask(
				mask.create(),
				chnlSrc
			);
		} else {
			return createWithoutMask(chnlSrc);
		}
	}
	
	private ObjMaskCollection createWithMask( BinaryChnl mask, Chnl chnl ) throws CreateException {
		
		VoxelBox<ByteBuffer> maskVb;
		try {
			maskVb = mask.getChnl().getVoxelBox().asByte();
		} catch (IncorrectVoxelDataTypeException e1) {
			throw new CreateException("binaryImgChnlProviderMask has incorrect data type", e1);
		}
		
		ObjMask om = new ObjMask( new BinaryVoxelBoxByte(maskVb, mask.getBinaryValues()) );
		
		SeedCollection seeds = null;
		if (objsSeeds!=null) {
			ObjMaskCollection seedsObjs = objsSeeds.create();
			ObjMask maskAsObject = CreateFromEntireChnlFactory.createObjMask(mask);
			seeds = SeedsFactory.createSeedsWithMask(
				seedsObjs,
				maskAsObject,
				new Point3i(0,0,0),
				chnl.getDimensions()
			);
		}
		
		try {
			return sgmn.sgmn(chnl, om, seeds);
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private ObjMaskCollection createWithoutMask(Chnl chnl) throws CreateException {
		
		SeedCollection seeds = null;
		
		try {
			if (objsSeeds!=null) {
				ObjMaskCollection seedsObjs = objsSeeds.create();
				seeds = SeedsFactory.createSeedsWithoutMask(seedsObjs);
			}			
			
			return sgmn.sgmn(chnl, seeds);
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
		
	}

	public ObjMaskSgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(ObjMaskSgmn sgmn) {
		this.sgmn = sgmn;
	}

	public ObjMaskProvider getObjsSeeds() {
		return objsSeeds;
	}

	public void setObjsSeeds(ObjMaskProvider objsSeeds) {
		this.objsSeeds = objsSeeds;
	}

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
