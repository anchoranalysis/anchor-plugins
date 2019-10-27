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
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposerOption;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernel;

public class KernelProposerOptionDual<T> extends KernelProposerOption<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6288955673726367708L;
	
	// START BEAN PROPERTIES
	@BeanField
	private KernelPosNeg<T> kernelPositive = null;
	
	@BeanField
	private KernelPosNeg<T> kernelNegative = null;
	
	@BeanField @NonNegative
	private double weightPos = -1;
	
	@BeanField @NonNegative
	private double weightNeg = -1;
	// END BEAN PROPERTIES
	
	public KernelProposerOptionDual() {
		
	}
	
	@Override
	public double getWeightPositive() {
		return weightPos;
	}

	public void setWeightPositive(double weightPos) {
		this.weightPos = weightPos;
	}

	@Override
	public double getWeightNegative() {
		return weightNeg;
	}

	public void setWeightNegative(double weightNeg) {
		this.weightNeg = weightNeg;
	}
	
	
	@Override
	// Add weighted kernel factories to a list, and returns the total weight
	public double addWeightedKernelFactories( List<WeightedKernel<T>> lst ) {
		
		kernelPositive.setProbPos(weightPos);
		kernelPositive.setProbNeg(weightNeg);
		
		kernelNegative.setProbPos(weightNeg);
		kernelNegative.setProbNeg(weightPos);
		
		lst.add( new WeightedKernel<T>( kernelPositive, weightPos ));
		lst.add( new WeightedKernel<T>( kernelNegative, weightNeg ));
		return getWeightPositive() + getWeightNegative();	
	}

	public KernelPosNeg<T> getKernelPositive() {
		return kernelPositive;
	}

	public void setKernelPositive(KernelPosNeg<T> kernelPositive) {
		this.kernelPositive = kernelPositive;
	}

	public KernelPosNeg<T> getKernelNegative() {
		return kernelNegative;
	}

	public void setKernelNegative(KernelPosNeg<T> kernelNegative) {
		this.kernelNegative = kernelNegative;
	}
}
