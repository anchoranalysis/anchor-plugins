package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme.termination;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.termination.TerminationCondition;

/**
 * 
 * TODO consider renaming to ConstantScore
 * 
 * @author FEEHANO
 *
 */
public class ConstantNRG extends TerminationCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5231021113947555019L;

	// START BEAN PARAMETERS
	@BeanField
	private int toleranceLog10 = -1;
	
	@BeanField @Positive
	private int numRep = -1;
	// END BEAN PARAMETERS
	
	private double tolerance =-1;
	
	private double prevNRG = 0;
	
	private int rep = 0;
	
	
	//private static Log log = LogFactory.getLog(ConstantNRG.class);
	
	public ConstantNRG() {
		super();
	}
	
	@Override
	public boolean continueIterations(int crntIter, double score, int size, LogReporter logReporter ) {
		
		// We increase our repetition counter, if the energy total is identical to the last time
		if (Math.abs( score - prevNRG) < this.tolerance ) {
			rep++;
		} else {
			rep = 0;
		}

		prevNRG = score;

		if (rep < numRep) {
			return true;
		} else {
			logReporter.logFormatted("ConstantNRG returned false at iter=%d", crntIter );
			return false;
		}
	}

	@Override
	public void init() {
		super.init();
		this.tolerance = Math.pow( 10.0, toleranceLog10 );
	}

	public int getToleranceLog10() {
		return toleranceLog10;
	}

	public void setToleranceLog10(int toleranceLog10) {
		this.toleranceLog10 = toleranceLog10;
	}

	public int getNumRep() {
		return numRep;
	}


	public void setNumRep(int numRep) {
		this.numRep = numRep;
	}
}