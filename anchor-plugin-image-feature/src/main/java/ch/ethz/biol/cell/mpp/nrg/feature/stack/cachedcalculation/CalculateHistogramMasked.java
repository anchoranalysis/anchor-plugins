package ch.ethz.biol.cell.mpp.nrg.feature.stack.cachedcalculation;

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

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/** Calculated a histogram for a specific region on a channel, as identified by a mask in another channel */
public class CalculateHistogramMasked extends FeatureCalculation<Histogram, FeatureInputStack> {

	private int nrgIndexSignal;
	private int nrgIndexMask;
	
	/**
	 * Constructor
	 * 
	 * @param nrgIndexSignal the index in the nrg-stack of the channel part of whose signal will form a histogram
	 * @param nrgIndexMask the index in the nrg-stack of a channel which is a binary mask (0=off, 255=on)
	 */
	public CalculateHistogramMasked( int nrgIndexSignal, int nrgIndexMask ) {
		super();
		this.nrgIndexSignal = nrgIndexSignal;
		this.nrgIndexMask = nrgIndexMask;
	}

	@Override
	protected Histogram execute( FeatureInputStack input ) throws FeatureCalcException {

		try {
			NRGStack nrgStack = input.getNrgStackRequired().getNrgStack();
			Channel chnl = extractChnl(nrgStack);
			
			return HistogramFactory.create(
				chnl,
				extractMask(nrgStack)
			);
			
		} catch (CreateException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateHistogramMasked){
	    	final CalculateHistogramMasked other = (CalculateHistogramMasked) obj;
	        return new EqualsBuilder()
	            .append(nrgIndexSignal, other.nrgIndexSignal)
	            .append(nrgIndexMask, other.nrgIndexMask)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(nrgIndexSignal)
				.append(nrgIndexMask)				
				.toHashCode();
	}
	
	private Channel extractChnl( NRGStack nrgStack ) throws FeatureCalcException {
		return nrgStack.getChnl(nrgIndexSignal);
	}
	
	private BinaryChnl extractMask( NRGStack nrgStack ) {
		Channel chnl = nrgStack.getChnl(nrgIndexMask);
		return new BinaryChnl(chnl, BinaryValues.getDefault() );
	}
}
