package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryImgChnlProviderThrshld extends BinaryImgChnlProvider {

	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private Thresholder thresholder;
	// END BEAN
	
	@Override
	public BinaryChnl create() throws CreateException {

		Chnl chnl = chnlProvider.create();
		
		BinaryValuesByte bvOut = BinaryValuesByte.getDefault();
		try {
			BinaryVoxelBox<ByteBuffer> bvb = thresholder.threshold( chnl.getVoxelBox(), bvOut, null );
			return new BinaryChnl(
				bvb,
				chnl.getDimensions().getRes(),
				ChnlFactory.instance().get(VoxelDataTypeUnsignedByte.instance)
			);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public Thresholder getThresholder() {
		return thresholder;
	}

	public void setThresholder(Thresholder thresholder) {
		this.thresholder = thresholder;
	}

}
