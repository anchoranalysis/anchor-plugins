package ch.ethz.biol.cell.mpp.nrg.feature.stack;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBoxByte;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.IncorrectVoxelDataTypeException;

public class BinaryImageChnlProviderFeature extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Feature<FeatureObjMaskParams> item;
	
	@BeanField
	@SkipInit
	private BinaryImgChnlProvider binaryImgChnlProvider;
	// END BEAN PROPERTIES
	
	@Override
	public double calcCast(CacheableParams<FeatureStackParams> params) throws FeatureCalcException {

		BinaryChnl bc;
		try {
			binaryImgChnlProvider.initRecursive(params.getParams().getSharedObjs(), getLogger() );
			bc = binaryImgChnlProvider.create();
		} catch (InitException | CreateException e1) {
			throw new FeatureCalcException(e1);
		}
		
		BinaryVoxelBox<ByteBuffer> bvb = binaryVoxelBox(bc);
		
		return params.calcChangeParams(
			item,
			p -> deriveParams(p, bvb),
			"binaryMask"
		);
	}
	
	private static FeatureObjMaskParams deriveParams(FeatureStackParams params, BinaryVoxelBox<ByteBuffer> bvb ) {
		FeatureObjMaskParams paramsObj = new FeatureObjMaskParams();
		paramsObj.setNrgStack( params.getNrgStack() );
		paramsObj.setObjMask(
			new ObjMask(bvb)
		);
		return paramsObj;
	}
	
	private static BinaryVoxelBox<ByteBuffer> binaryVoxelBox( BinaryChnl bic ) throws FeatureCalcException {
		VoxelBox<ByteBuffer> vb;
		try {
			vb = bic.getChnl().getVoxelBox().asByte();
		} catch (IncorrectVoxelDataTypeException e1) {
			throw new FeatureCalcException("binaryImgChnlProvider returned incompatible data type", e1);
		}
		
		return new BinaryVoxelBoxByte( vb, bic.getBinaryValues() );
	}

	public Feature<FeatureObjMaskParams> getItem() {
		return item;
	}

	public void setItem(Feature<FeatureObjMaskParams> item) {
		this.item = item;
	}


	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}


	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}
}
