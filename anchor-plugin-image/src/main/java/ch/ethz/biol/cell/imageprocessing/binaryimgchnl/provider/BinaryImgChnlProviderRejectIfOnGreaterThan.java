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
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;

public class BinaryImgChnlProviderRejectIfOnGreaterThan extends BinaryImgChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField
	private double maxRatioOn;
	
	@BeanField
	private boolean bySlice = false;		// Rejects if any slice has more On than the maxRatio allows
	// END BEAN PROPERTIES
	
	@Override
	public BinaryChnl create() throws CreateException {
		BinaryChnl binaryImgChnl = binaryImgChnlProvider.create();
		
		if (bySlice) {
			for( int z=0; z<binaryImgChnl.getDimensions().getZ(); z++) {
				testChnl( binaryImgChnl.extractSlice(z) );
			}
		} else {
			testChnl(binaryImgChnl);
		}
		return binaryImgChnl;
	}
	
	private void testChnl( BinaryChnl binaryImgChnl ) throws CreateException {
		int cnt = binaryImgChnl.countHighValues();
		int volume = binaryImgChnl.getDimensions().getVolume();
		
		double ratio = ((double) cnt) / volume;
		
		if (ratio>maxRatioOn) {
			throw new CreateException(
				String.format("binaryImgChnl has on-pixel ratio of %f when a max of %f is allowed",ratio,maxRatioOn)
			);
		}
	}
	
	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public double getMaxRatioOn() {
		return maxRatioOn;
	}

	public void setMaxRatioOn(double maxRatioOn) {
		this.maxRatioOn = maxRatioOn;
	}

	public boolean isBySlice() {
		return bySlice;
	}

	public void setBySlice(boolean bySlice) {
		this.bySlice = bySlice;
	}
}
