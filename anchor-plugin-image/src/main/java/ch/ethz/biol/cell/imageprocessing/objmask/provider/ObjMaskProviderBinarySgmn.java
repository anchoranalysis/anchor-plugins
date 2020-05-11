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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProviderOne;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ObjMaskProviderBinarySgmn extends ObjMaskProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn binarySgmn;
	
	@BeanField
	private ChnlProvider chnlProvider;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection objMasks) throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		
		ObjMaskCollection masksOut = new ObjMaskCollection();
		try {
			
			for( ObjMask om : objMasks ) {
				VoxelBox<?> vb = chnl.getVoxelBox().any().createBufferAvoidNew(om.getBoundingBox() );
				
				BinaryVoxelBox<ByteBuffer> bvb = binarySgmn.sgmn(
					new VoxelBoxWrapper(vb),
					new BinarySgmnParameters(),
					new ObjMask( om.getVoxelBox())
				);
				
				ObjMask mask = new ObjMask( om.getBoundingBox(), bvb.getVoxelBox() );
				mask.setBinaryValues( bvb.getBinaryValues() );
				
				masksOut.add(mask);
			}
		
		
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
		
		return masksOut;
	}

	public BinarySgmn getBinarySgmn() {
		return binarySgmn;
	}

	public void setBinarySgmn(BinarySgmn binarySgmn) {
		this.binarySgmn = binarySgmn;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

}
