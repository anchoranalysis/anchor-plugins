package ch.ethz.biol.cell.imageprocessing.io.chnlconverter.bean;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.chnl.converter.ChnlConverterBean;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedByteScaleByMinMaxValue;

/**
 * Scales by compressing a certain range of values into the 8-bit signal
 * 
 * @author FEEHANO
 *
 */
public class ChnlConverterBeanScaleByKeyValueParams extends ChnlConverterBean {

	// START BEAN PROPERTIES 
	@BeanField
	private KeyValueParamsProvider keyValueParamsProvider;
	
	@BeanField
	private String keyLower;
	
	@BeanField
	private String keyUpper;
	
	@BeanField
	private double scaleLower;
	
	@BeanField
	private double scaleUpper;
	// END BEAN PROPERTIES
	
	@Override
	public ChnlConverter<?> createConverter() throws CreateException {
		
		KeyValueParams kvp = keyValueParamsProvider.create();
		
		int min = getScaled( kvp, keyLower, scaleLower );
		int max = getScaled( kvp, keyUpper, scaleUpper ); 
		
		getLogger().getLogReporter().logFormatted("ChnlConverter: scale with min=%d max=%d%n", min, max);
		
		return new ChnlConverterToUnsignedByteScaleByMinMaxValue(min, max);
	}
	
	private int getScaled( KeyValueParams kvp, String key, double scale ) throws CreateException {
		
		if (!kvp.containsKey(key)) {
			throw new CreateException(
				String.format("Params is missing key '%s'", key)
			);
		}
		
		double val = kvp.getPropertyAsDouble(key);
		
		getLogger().getLogReporter().logFormatted("%f * %f = %f", val, scale, val*scale);
		
		return (int) Math.round(val * scale );
	}

	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}

	public void setKeyValueParamsProvider(KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}

	public String getKeyLower() {
		return keyLower;
	}

	public void setKeyLower(String keyLower) {
		this.keyLower = keyLower;
	}

	public String getKeyUpper() {
		return keyUpper;
	}

	public void setKeyUpper(String keyUpper) {
		this.keyUpper = keyUpper;
	}

	public double getScaleLower() {
		return scaleLower;
	}

	public void setScaleLower(double scaleLower) {
		this.scaleLower = scaleLower;
	}

	public double getScaleUpper() {
		return scaleUpper;
	}

	public void setScaleUpper(double scaleUpper) {
		this.scaleUpper = scaleUpper;
	}

}
