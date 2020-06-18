package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;



import java.util.Optional;

import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;

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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.mpp.sgmn.bean.kernel.proposer.KernelProposer;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.OptScheme;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeContext;
import org.anchoranalysis.mpp.sgmn.optscheme.OptTerminatedEarlyException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;

public class OptSchemeUnifPerm<S> extends OptScheme<S,S> {

	// START BEAN PROPERTIES
	@BeanField
	private int numItr = -1;
	
	@BeanField
	private ExtractScoreSize<S> extractScoreSize;
	// END BEAN PROPERTIES
	
	public OptSchemeUnifPerm() {
		
	}
	
	public OptSchemeUnifPerm(int numItr) {
		super();
		this.numItr = numItr;
	}

	// Finds an optimum by generating a certain number of configurations
	@Override
	public S findOpt(
		KernelProposer<S> kernelProposer,
		ListUpdatableMarkSetCollection updatableMarkSetCollection,
		FeedbackReceiver<S> feedbackReceiver,
		OptSchemeContext initContext
	) throws OptTerminatedEarlyException {
		
		S best = null;
		
		KernelCalcContext context = initContext.calcContext( initContext.cfgGenContext() );
			
		try {
			kernelProposer.initBeforeCalc(context);
			
			for( int i=0; i<this.numItr; i++) {
				Optional<S> cfgNRG = kernelProposer.getInitialKernel().makeProposal(
					Optional.empty(),
					context
				);
				
				if (!cfgNRG.isPresent()) {
					continue;
				}
				
				// We find the maximum
				if( best==null || extractScore(cfgNRG.get()) > extractScore(best) ) {
					best = cfgNRG.get();
				}
			}
		} catch (KernelCalcNRGException | InitException e) {
			throw new OptTerminatedEarlyException("Optimization terminated early", e);
		}
	
		return best;
	}
	
	private double extractScore( S item ) {
		return extractScoreSize.extractScore(item);
	}

	public int getNumItr() {
		return numItr;
	}

	public void setNumItr(int numItr) {
		this.numItr = numItr;
	}

	public ExtractScoreSize<S> getExtractScoreSize() {
		return extractScoreSize;
	}

	public void setExtractScoreSize(ExtractScoreSize<S> extractScoreSize) {
		this.extractScoreSize = extractScoreSize;
	}

}
