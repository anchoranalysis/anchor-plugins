package org.anchoranalysis.plugin.mpp.experiment.bean.outputmanager;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
import org.anchoranalysis.io.output.bean.OutputManagerWithPrefixer;
import org.anchoranalysis.io.output.bean.allowed.AllOutputAllowed;
import org.anchoranalysis.io.output.bean.allowed.NoOutputAllowed;
import org.anchoranalysis.io.output.bean.allowed.OutputAllowed;
import org.anchoranalysis.mpp.io.output.StackOutputKeys;

import lombok.Getter;
import lombok.Setter;

public class OutputManagerStack extends OutputManagerWithPrefixer {
	
	// BEAN PROPERTIES
	/** What's allowed or not - highest level outputs */
	@BeanField @Getter @Setter
	private OutputAllowed outputEnabled = new AllOutputAllowed();
	
	/** What's allowed or not when outputting stacks */
	@BeanField @Getter @Setter
	private OutputAllowed stackCollectionOutputEnabled = new AllOutputAllowed();
	
	/** What's allowed or not when outputting configurations */
	@BeanField @Getter @Setter
	private OutputAllowed cfgCollectionOutputEnabled = new AllOutputAllowed();
	
	/** What's allowed or not when outputting object-collections */
	@BeanField @Getter @Setter
	private OutputAllowed objMaskCollectionOutputEnabled = new AllOutputAllowed();
	
	/** What's allowed or not when outputting histograms */
	@BeanField @Getter @Setter
	private OutputAllowed histogramCollectionOutputEnabled = new AllOutputAllowed(); 
	// END BEAN PROPERTIES

	@Override
	public OutputAllowed outputAllowedSecondLevel(String key) {
		switch(key) {
		case StackOutputKeys.STACK:
			return getStackCollectionOutputEnabled();
		case StackOutputKeys.CFG:
			return getCfgCollectionOutputEnabled();
		case StackOutputKeys.HISTOGRAM:
			return getHistogramCollectionOutputEnabled();
		case StackOutputKeys.OBJS:
			return getObjMaskCollectionOutputEnabled();			
		default:
			return new NoOutputAllowed();
		}
	}
		
	@Override
	public boolean isOutputAllowed( String outputName ) {
		return outputEnabled.isOutputAllowed(outputName);
	}
}
