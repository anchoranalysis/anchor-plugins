package org.anchoranalysis.plugin.mpp.sgmn.bean.kernel.proposer;

/*
 * #%L
 * anchor-plugin-mpp
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


import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposerOption;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernel;


public class KernelProposerOptionSingle<T> extends KernelProposerOption<T>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6722347375925588035L;
	
	// START BEAN PROPERTIES
	@BeanField
	private Kernel<T> kernel = null;
	
	@BeanField
	private double weight = 0;
	// END BEAN PROPERTIES
	
	public KernelProposerOptionSingle() {
		
	}
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	
	
	// KernelProposerOption interface
	@Override
	public double getWeightPositive() {
		return weight;
	}
	
	@Override
	public double getWeightNegative() {
		return weight;
	}
	
	@Override
	// Add weighted kernel factories to a list, and returns the total weight
	public double addWeightedKernelFactories( List<WeightedKernel<T>> lst ) {
		lst.add( new WeightedKernel<T>( kernel, getWeight() ) );
		return getWeight();		
	}

	public Kernel<T> getKernel() {
		return kernel;
	}

	public void setKernel(Kernel<T> kernel) {
		this.kernel = kernel;
	}
}
