package ch.ethz.biol.cell.mpp.feedback;

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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.experiment.bean.RequireArguments;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class FeedbackReceiverRequireArguments<T> extends FeedbackReceiverBean<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField @Optional
	private FeedbackReceiverBean<T> feedbackReceiver;
	
	@BeanField
	private RequireArguments requireArguments;
	// END BEAN PROPERTIES
	
	private boolean doFeedback;

	@Override
	public void reportBegin(OptimizationFeedbackInitParams<T> optInit)
			throws ReporterException {
		
		doFeedback = requireArguments.hasAllRequiredArguments(
			optInit.getInitContext().getExperimentArguments()
		);
		
		if (!doFeedback) {
			return;
		}
		
		feedbackReceiver.reportBegin(optInit);
	}

	@Override
	public void reportItr(Reporting<T> reporting) throws ReporterException {
		
		if (!doFeedback) {
			return;
		}
		
		feedbackReceiver.reportItr(reporting);
	}

	@Override
	public void reportNewBest(Reporting<T> reporting)
			throws ReporterException {
		
		if (!doFeedback) {
			return;
		}
		
		feedbackReceiver.reportNewBest(reporting);
	}

	@Override
	public void reportEnd(OptimizationFeedbackEndParams<T> optStep)
			throws ReporterException {
		
		if (!doFeedback) {
			return;
		}
		
		feedbackReceiver.reportEnd(optStep);
	}

	public FeedbackReceiverBean<T> getFeedbackReceiver() {
		return feedbackReceiver;
	}

	public void setFeedbackReceiver(FeedbackReceiverBean<T> feedbackReceiver) {
		this.feedbackReceiver = feedbackReceiver;
	}

	public RequireArguments getRequireArguments() {
		return requireArguments;
	}

	public void setRequireArguments(RequireArguments requireArguments) {
		this.requireArguments = requireArguments;
	}


}
