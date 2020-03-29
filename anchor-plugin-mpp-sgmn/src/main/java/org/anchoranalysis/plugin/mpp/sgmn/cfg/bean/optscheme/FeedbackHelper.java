package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.mpp.sgmn.kernel.proposer.WeightedKernelList;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.OptSchemeInitContext;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;

class FeedbackHelper {

	public static <T> FeedbackGenerator<T> createInitFeedbackGenerator(
		FeedbackReceiver<T> feedbackReceiver,
		OptSchemeInitContext initContext,
		WeightedKernelList<?> allKernelFactories,
		ExtractScoreSize<T> extractScoreSize
	) {
		
		FeedbackGenerator<T> feedbackGenerator = new FeedbackGenerator<T>(
			feedbackReceiver,
			initContext.getLogger().getErrorReporter()
		);
		
		feedbackGenerator.begin(
			feedbackInitParams(initContext, allKernelFactories ),
			extractScoreSize
		);
		
		return feedbackGenerator;
	}
	
	public static <T> void endWithFinalFeedback(
		FeedbackGenerator<T> feedbackGenerator,
		T state,
		OptSchemeInitContext initContext
	) {
		OptimizationFeedbackEndParams<T> optEndParams = feedbackEndParams(
			state,
			initContext.getLogger().getLogReporter()
		);
		feedbackGenerator.end( optEndParams );
	}
	
	private static <T> OptimizationFeedbackInitParams<T> feedbackInitParams( OptSchemeInitContext initContext, WeightedKernelList<?> allKernelFactories ) {
		OptimizationFeedbackInitParams<T> feedbackParams = new OptimizationFeedbackInitParams<>();
		feedbackParams.setInitContext(initContext);
		feedbackParams.setKernelFactoryList( allKernelFactories );
		return feedbackParams;
	}
		
	private static <T> OptimizationFeedbackEndParams<T> feedbackEndParams( T state, LogReporter logger ) {
		OptimizationFeedbackEndParams<T> optEndParams = new OptimizationFeedbackEndParams<>();
		optEndParams.setState( state );
		optEndParams.setLogReporter( logger );
		return optEndParams;
	}
}
