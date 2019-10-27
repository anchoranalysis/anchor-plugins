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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.threshold.calculatelevel.CalculateLevel;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;

public class ChnlProviderCalculateLevel extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private CalculateLevel calculateLevel;
	
	@BeanField @Optional
	private BinaryImgChnlProvider histogramMaskProvider;
	// END BEAN
		
	@Override
	public Chnl create() throws CreateException {

		Chnl chnlIntensity = chnlProvider.create();
		
		Histogram h;
		if (histogramMaskProvider!=null) {
			h = HistogramFactoryUtilities.create(chnlIntensity,histogramMaskProvider.create());
		} else {
			h = HistogramFactoryUtilities.create(chnlIntensity);
		}
		
		try {
			int level = calculateLevel.calculateLevel(h);
			
			chnlIntensity.getVoxelBox().any().setAllPixelsTo(level);
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}

		return chnlIntensity;
	}

	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}


	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}


	public BinaryImgChnlProvider getHistogramMaskProvider() {
		return histogramMaskProvider;
	}


	public void setHistogramMaskProvider(BinaryImgChnlProvider histogramMaskProvider) {
		this.histogramMaskProvider = histogramMaskProvider;
	}

}
