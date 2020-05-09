package ch.ethz.biol.cell.imageprocessing.chnl.provider;

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
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;

public class ChnlProviderRejectIfNoSignal extends ChnlProvider {

	// START BEANS
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private int minIntensity = 40;
	
	@BeanField
	private double minRatio = 0.01;
	// END BEANS

	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		
		Histogram h = HistogramFactoryUtilities.create( chnl );
		
		double percent = h.percentGreaterEqualTo(minIntensity);
		
		if (percent < minRatio) {
			String msg = String.format("Rejecting as %f < %f for intensity %d", percent, minRatio, minIntensity );
			throw new CreateException( msg );
		}

		return chnl;
	}

	public int getMinIntensity() {
		return minIntensity;
	}

	public void setMinIntensity(int minIntensity) {
		this.minIntensity = minIntensity;
	}

	public double getMinRatio() {
		return minRatio;
	}

	public void setMinRatio(double minRatio) {
		this.minRatio = minRatio;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}


}
