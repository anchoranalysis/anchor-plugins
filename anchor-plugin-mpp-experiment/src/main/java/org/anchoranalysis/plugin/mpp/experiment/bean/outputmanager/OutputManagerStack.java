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

public class OutputManagerStack extends OutputManagerWithPrefixer {
	
	// BEAN PROPERTIES
	@BeanField
	private OutputAllowed outputEnabled = new AllOutputAllowed();		// What outputs are allowed and not allowed
	
	@BeanField
	private OutputAllowed stackCollectionOutputEnabled = new AllOutputAllowed();	// If we output a stackCollection, what's allowed and not
	
	@BeanField
	private OutputAllowed cfgCollectionOutputEnabled = new AllOutputAllowed();	// If we output a cfgCollection, what's allowed and not
	
	@BeanField
	private OutputAllowed objMaskCollectionOutputEnabled = new AllOutputAllowed();	// If we output a objMaskCollection, what's allowed and not
	
	@BeanField
	private OutputAllowed histogramCollectionOutputEnabled = new AllOutputAllowed();	// If we output a objMaskCollection, what's allowed and not
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
	


	public OutputAllowed getOutputEnabled() {
		return outputEnabled;
	}


	public void setOutputEnabled(OutputAllowed outputEnabled) {
		this.outputEnabled = outputEnabled;
	}

	public OutputAllowed getStackCollectionOutputEnabled() {
		return stackCollectionOutputEnabled;
	}


	public void setStackCollectionOutputEnabled(
			OutputAllowed stackCollectionOutputEnabled) {
		this.stackCollectionOutputEnabled = stackCollectionOutputEnabled;
	}


	public OutputAllowed getCfgCollectionOutputEnabled() {
		return cfgCollectionOutputEnabled;
	}


	public void setCfgCollectionOutputEnabled(
			OutputAllowed cfgCollectionOutputEnabled) {
		this.cfgCollectionOutputEnabled = cfgCollectionOutputEnabled;
	}


	public OutputAllowed getObjMaskCollectionOutputEnabled() {
		return objMaskCollectionOutputEnabled;
	}


	public void setObjMaskCollectionOutputEnabled(
			OutputAllowed objMaskCollectionOutputEnabled) {
		this.objMaskCollectionOutputEnabled = objMaskCollectionOutputEnabled;
	}


	public OutputAllowed getHistogramCollectionOutputEnabled() {
		return histogramCollectionOutputEnabled;
	}


	public void setHistogramCollectionOutputEnabled(
			OutputAllowed histogramCollectionOutputEnabled) {
		this.histogramCollectionOutputEnabled = histogramCollectionOutputEnabled;
	}
	// END BEAN getters and setters

}
