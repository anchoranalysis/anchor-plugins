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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.init.ImageInitParams;

public class BinaryImgChnlProviderStackReference extends BinaryChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private int chnlIndex = 0;
	
	@BeanField
	private String stackProviderID;
	// END BEAN PROPERTIES
	
	private Chnl chnl;
	
	@Override
	public void onInit(ImageInitParams so)
			throws InitException {
		super.onInit(so);
		try {
			chnl = so.getStackCollection().getException(stackProviderID).getChnl(chnlIndex);
		} catch (NamedProviderGetException e) {
			throw InitException.createOrReuse(e.summarize());
		}
	}

	@Override
	public BinaryChnl create() throws CreateException {
		return new BinaryChnl( chnl, BinaryValues.getDefault() );
	}

	public int getChnlIndex() {
		return chnlIndex;
	}

	public void setChnlIndex(int chnlIndex) {
		this.chnlIndex = chnlIndex;
	}

	public String getStackProviderID() {
		return stackProviderID;
	}

	public void setStackProviderID(String stackProviderID) {
		this.stackProviderID = stackProviderID;
	}

}
