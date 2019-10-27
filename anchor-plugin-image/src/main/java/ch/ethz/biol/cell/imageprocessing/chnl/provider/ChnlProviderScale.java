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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.image.bean.interpolator.InterpolatorBean;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.scale.ScaleFactor;

import anchor.image.bean.interpolator.InterpolatorBeanLanczos;

public class ChnlProviderScale extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Start BEAN PROPERTIES
	@BeanField
	private ScaleCalculator scaleCalculator;
	
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private InterpolatorBean interpolator = new InterpolatorBeanLanczos();
	// End BEAN PROPERTIES
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		return scale( chnl, scaleCalculator, interpolator.create(), getLogger() );
	}
	
	// logErrorReporter can be null
	public static Chnl scale( Chnl chnl, ScaleCalculator scaleCalculator, Interpolator interpolator, LogErrorReporter logErrorReporter ) throws CreateException {
		try {
			if (logErrorReporter!=null) {
				logErrorReporter.getLogReporter().logFormatted("Res in: %s\n", chnl.getDimensions().getRes() );
			}
			
			ScaleFactor sf = scaleCalculator.calc( chnl.getDimensions() );
			
			if (logErrorReporter!=null) {
				logErrorReporter.getLogReporter().logFormatted("ScaleFactor: %s\n", sf.toString() );
			}
			
			Chnl chnlOut = chnl.scaleXY( sf.getX(), sf.getY(), interpolator);
			
			if (logErrorReporter!=null) {
				logErrorReporter.getLogReporter().logFormatted("Res out: %s\n", chnlOut.getDimensions().getRes() );
			}
			
			return chnlOut;
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	public ScaleCalculator getScaleCalculator() {
		return scaleCalculator;
	}

	public void setScaleCalculator(ScaleCalculator scaleCalculator) {
		this.scaleCalculator = scaleCalculator;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public InterpolatorBean getInterpolator() {
		return interpolator;
	}

	public void setInterpolator(InterpolatorBean interpolator) {
		this.interpolator = interpolator;
	}



}
