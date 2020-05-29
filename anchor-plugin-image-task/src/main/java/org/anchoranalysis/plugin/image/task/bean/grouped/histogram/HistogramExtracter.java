package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;

/*-
 * #%L
 * anchor-plugin-image-task
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.plugin.image.task.grouped.ChnlSource;

/** Extracts a histogram from an image for a given key */
class HistogramExtracter {

	private ChnlSource source;
	private String keyMask;
	private int maskValue;
		
	public HistogramExtracter(
		ChnlSource source,
		String keyMask,
		int maskValue 
	) throws JobExecutionException {
		super();
		this.source = source;
		this.keyMask = keyMask;
		this.maskValue = maskValue;
	}
	
	public Histogram extractFrom( Chnl chnl ) throws JobExecutionException {
		
		try {
			if (!keyMask.isEmpty()) {
				BinaryChnl mask = extractMask(keyMask);
				return HistogramFactory.create( chnl, mask );
			} else {
				return HistogramFactory.create( chnl );	
			}
			
		} catch ( CreateException e) {
			throw new JobExecutionException("Cannot create histogram", e);
		}
	}
		
	private BinaryChnl extractMask( String stackName ) throws JobExecutionException {
		try {
			Chnl chnl = source.extractChnl(stackName, false);
			return new BinaryChnl(chnl, createMaskBinaryValues() );
			
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private BinaryValues createMaskBinaryValues() throws JobExecutionException {
		if (maskValue==255) {
			return new BinaryValues(0, 255);
		} else if (maskValue==0) {
			return new BinaryValues(255, 0);
		} else {
			throw new JobExecutionException("Only mask-values of 255 or 0 are current supported");
		}
	}
}
