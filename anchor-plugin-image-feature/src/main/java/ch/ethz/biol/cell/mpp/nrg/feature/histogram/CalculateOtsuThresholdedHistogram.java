package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

/*-
 * #%L
 * anchor-plugin-image-feature
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

import org.anchoranalysis.bean.init.params.NullInitParams;
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramParams;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.plugin.image.bean.threshold.HistogramThresholder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

class CalculateOtsuThresholdedHistogram extends CachedCalculation<Histogram, FeatureHistogramParams> {

	private CalculateLevel calculateLevel;
	
	private LogErrorReporter logger;
	
	public CalculateOtsuThresholdedHistogram(CalculateLevel calculateLevel, LogErrorReporter logger ) {
		super();
		this.calculateLevel = calculateLevel;
		this.logger = logger;
	}

	@Override
	protected Histogram execute(FeatureHistogramParams params) throws ExecuteException {
		try {
			if (!calculateLevel.isHasBeenInit()) {
				calculateLevel.init( NullInitParams.instance(), logger);
			}
			return HistogramThresholder.withCalculateLevel(
				params.getHistogram().duplicate(),	// Important to duplicate
				calculateLevel
			) ;
		} catch (OperationFailedException | InitException e) {
			throw new ExecuteException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CalculateOtsuThresholdedHistogram){
	    	final CalculateOtsuThresholdedHistogram other = (CalculateOtsuThresholdedHistogram) obj;
	        return new EqualsBuilder()
	            .append(calculateLevel, other.calculateLevel)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(calculateLevel)
			.toHashCode();
	}
}
