package ch.ethz.biol.cell.sgmn.graphcuts.nrgdefinition.pixelscore;

import java.util.List;
import java.util.Optional;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.histogram.Histogram;

public abstract class PixelScoreCalculateLevelBase extends PixelScoreSingleChnl {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private CalculateLevel calculateLevel;
	
	@BeanField
	private int histChnlIndex = 0;
	// END BEAN PROPERTIES
	
	private int level;
		
	@Override
	public void init( List<Histogram> histograms, Optional<KeyValueParams> keyValueParams) throws InitException {
		
		try {
			Histogram hist = histograms.get(histChnlIndex);
			level = calculateLevel.calculateLevel(hist);
			
			beforeCalcSetup(hist, level);
		} catch (OperationFailedException e) {
			throw new InitException(e);
		}
	}

	@Override
	protected double deriveScoreFromPixelVal(int pixelVal) {
		return calcForPixel(
			pixelVal,
			level
		);
	}

	protected abstract void beforeCalcSetup( Histogram hist, int level ) throws OperationFailedException;	
	
	protected abstract double calcForPixel( int pxlValue, int level );
	
	public CalculateLevel getCalculateLevel() {
		return calculateLevel;
	}

	public void setCalculateLevel(CalculateLevel calculateLevel) {
		this.calculateLevel = calculateLevel;
	}

	public int getHistChnlIndex() {
		return histChnlIndex;
	}

	public void setHistChnlIndex(int histChnlIndex) {
		this.histChnlIndex = histChnlIndex;
	}




}
