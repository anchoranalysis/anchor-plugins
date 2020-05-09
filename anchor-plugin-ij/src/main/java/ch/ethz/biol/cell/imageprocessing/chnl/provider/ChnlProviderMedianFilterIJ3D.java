package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-ij
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


import ij.ImagePlus;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.IJWrap;

public class ChnlProviderMedianFilterIJ3D extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	// END BEAN PROPERTIES
	
	private Chnl median3d( Chnl chnl ) throws CreateException {
		
		ImagePlus imp = IJWrap.createImagePlus(chnl);
		
		Hybrid_3D_Median_Filter plugin = new Hybrid_3D_Median_Filter();
		plugin.setup("", imp);
		
		ImagePlus impOut = plugin.Hybrid3dMedianizer( imp );
		
		return IJWrap.chnlFromImagePlus(
			impOut,
			chnl.getDimensions().getRes()
		);
	}
	
	@Override
	public Chnl create() throws CreateException {
		return median3d(chnlProvider.create());
	}


	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}




}
