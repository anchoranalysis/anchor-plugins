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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.chnl.converter.histogram.ChnlConverterHistogramBean;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.stack.region.chnlconverter.attached.ChnlConverterAttached;

// Converts a chnl by applying a ChnlConverter. A histogram is used to determine the conversion. This is either
// calculated automatically from the incoming channel, or provided explicitly.
public class ChnlProviderConverterHistogram extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private HistogramProvider histogramProvider;	// If null, the histogram is calculated from the image
	
	@BeanField
	private ChnlConverterHistogramBean chnlConverter;
	
	@BeanField
	private boolean changeExisting = false;	// If true, the existing channel can be changed. If false, a new channel must be created for any change
	// END BEAN PROPERTIES
	
	@Override
	public Chnl createFromChnl( Chnl chnl ) throws CreateException {

		assert(chnl!=null);
		
		Histogram hist;
		if (histogramProvider!=null) {
			hist = histogramProvider.create();
		} else {
			hist = HistogramFactoryUtilities.create(chnl);
		}
		
		ChnlConverterAttached<Histogram, ?> converter = chnlConverter.createConverter();
		
		try {
			converter.attachObject(hist);
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
		
		ConversionPolicy conversionPolicy = changeExisting ? ConversionPolicy.CHANGE_EXISTING_CHANNEL : ConversionPolicy.DO_NOT_CHANGE_EXISTING;
		chnl = converter.convert(chnl, conversionPolicy );
		return chnl;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}

	public ChnlConverterHistogramBean getChnlConverter() {
		return chnlConverter;
	}

	public void setChnlConverter(ChnlConverterHistogramBean chnlConverter) {
		this.chnlConverter = chnlConverter;
	}

	public boolean isChangeExisting() {
		return changeExisting;
	}

	public void setChangeExisting(boolean changeExisting) {
		this.changeExisting = changeExisting;
	}
	
}
