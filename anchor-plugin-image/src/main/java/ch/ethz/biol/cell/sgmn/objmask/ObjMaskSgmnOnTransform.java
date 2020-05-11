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
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;

public class ObjMaskSgmnOnTransform extends ObjMaskSgmn {
	
	/* START Bean properties */
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField
	private ChnlProvider chnlProviderTransform;
	
	@BeanField
	private ObjMaskSgmn sgmnObj;
	/* END Bean properties */
	
	// Performs the segmentation
	@Override
	public ObjMaskCollection sgmn( Chnl chnl, SeedCollection seeds ) throws SgmnFailedException {

		try {
			BinarySgmnParameters params = new BinarySgmnParameters(
				chnl.getDimensions().getRes()
			);
			
			BinaryVoxelBox<ByteBuffer> bvb = sgmn.sgmn(
				chnl.getVoxelBox(),
				params
			);

			// DEBUG
			// getOutputManager().getWriterCheckIfAllowed().write("afterSgmn", new ChnlGenerator(chnl,"afterSgmn") );
			
			Chnl chnlEDM = chnlProviderTransform.create();
			
			// DEBUG
			// getOutputManager().getWriterCheckIfAllowed().write("edm", new ChnlGenerator(chnlEDM,"edm") );
			
			// If we have seeds, we impose minima
//			if (seeds!=null) {
//				MinimaImposition2D minimaImposition = new MinimaImposition2D();
//				chnlEDM = minimaImposition.imposeMinima(chnlEDM, seeds, new ObjMask(chnl.getVoxelBox()));
//				getOutputManager().write("edmRecon", new ChnlGenerator<ByteBuffer>(chnlEDM,"edm") );
//			}
			// We impose minima
			
			return sgmnObj.sgmn(chnlEDM, new ObjMask( bvb ), seeds);
			
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
	}
	
//	private ObjMaskCollection doWatershed( ImgChnl chnl ) throws SgmnFailedException {
//		EDM edmPlugin = new EDM();
//		log.debug("Watershed started");
//		edmPlugin.toWatershed( chnl.getVoxelBox().imageProcessor(0) );
//		log.debug("Watershed finished");
//		
//		getOutputManager().write("wshed", new ChnlGenerator<ByteBuffer>(chnl,"watershed") );
//		
//		return sgmnObj.sgmn(chnl, null);
//	}
	
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			SeedCollection seeds) throws SgmnFailedException {
		throw new SgmnFailedException("Unsupported operation");
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn binarySgmn) {
		this.sgmn = binarySgmn;
	}

	public ObjMaskSgmn getSgmnObj() {
		return sgmnObj;
	}

	public void setSgmnObj(ObjMaskSgmn sgmnObj) {
		this.sgmnObj = sgmnObj;
	}


	public ChnlProvider getChnlProviderTransform() {
		return chnlProviderTransform;
	}


	public void setChnlProviderTransform(ChnlProvider chnlProviderTransform) {
		this.chnlProviderTransform = chnlProviderTransform;
	}
}
