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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.unit.SpatialConversionUtilities;
import org.anchoranalysis.core.unit.SpatialConversionUtilities.UnitSuffix;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ImageUnitConverter;
import org.anchoranalysis.image.orientation.DirectionVector;

public class ChnlProviderIJBackgroundSubtractorFromParams extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private KeyValueParamsProvider keyValueParamsProvider;
	
	@BeanField
	private String keyRadiusMicrons;
	// END BEAN PROPERTIES
		
	@Override
	public Chnl create() throws CreateException {
		
		KeyValueParams params = keyValueParamsProvider.create();
		
		if (params==null) {
			throw new CreateException("keyValueParamsProvider returned null");
		}
		
		if (!params.containsKey(keyRadiusMicrons)) {
			throw new CreateException(String.format("There is no key '%s'",keyRadiusMicrons));
		}
		
		Chnl chnl = chnlProvider.create();
		
		double radiusMicrons = Double.parseDouble( params.getProperty(keyRadiusMicrons));
		double radiusMeters = SpatialConversionUtilities.convertFromUnits(radiusMicrons, UnitSuffix.MICRO);	

		
		DirectionVector unitVectorXAxis = new DirectionVector(1.0, 0.0, 0.0);
		double radiusPixelsDouble = ImageUnitConverter.convertFromPhysicalDistance(radiusMeters, chnl.getDimensions().getRes(), unitVectorXAxis);
		
		int radiusPixels = (int) Math.round(radiusPixelsDouble);
		
		getLogger().getLogReporter().logFormatted("Subtracting background with radius=%d pixels (%f microns)", radiusPixels, radiusMicrons );
		
		return ChnlProviderIJBackgroundSubtractor.subtractBackground(chnl, radiusPixels, true);
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}

	public void setKeyValueParamsProvider(
			KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}

	public String getKeyRadiusMicrons() {
		return keyRadiusMicrons;
	}

	public void setKeyRadiusMicrons(String keyRadiusMicrons) {
		this.keyRadiusMicrons = keyRadiusMicrons;
	}



}
