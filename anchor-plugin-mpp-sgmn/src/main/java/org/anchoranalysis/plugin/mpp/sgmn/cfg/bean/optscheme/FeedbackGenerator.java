package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.optscheme;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.ExtractScoreSize;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.FeedbackReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregateTriggerBank;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.period.PeriodTriggerBank;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

// NB we execute every feedback action in a separate thread
//  on the Swing's event dispatch thread
class FeedbackGenerator<T> {
	
	private PeriodTriggerBank<T> periodTriggerBank;
	private AggregateTriggerBank<T> aggregateTriggerBank;
	private FeedbackReceiver<T> feedbackReceiver;
	private ErrorReporter errorReporter;
	
	public FeedbackGenerator(FeedbackReceiver<T> feedbackReceiver, ErrorReporter errorReporter) {
		super();
		this.feedbackReceiver = feedbackReceiver;
		this.errorReporter = errorReporter;
	}

	public void begin( OptimizationFeedbackInitParams<T> initParams, ExtractScoreSize<T> extracter ) {
	
		this.periodTriggerBank = new PeriodTriggerBank<>();
		this.aggregateTriggerBank = new AggregateTriggerBank<>(extracter);
		
		initParams.setPeriodTriggerBank( periodTriggerBank );
		initParams.setAggregateTriggerBank( aggregateTriggerBank );
		
		try {
			feedbackReceiver.reportBegin( initParams );
	
			aggregateTriggerBank.start(initParams);
		} catch (AggregatorException | ReporterException e) {
			errorReporter.recordError(FeedbackGenerator.class, e);
		}
		periodTriggerBank.reset();
	}
	
	public void recordBest( Reporting<T> reporting ) {
		
		try {
			feedbackReceiver.reportNewBest( reporting );
		} catch (ReporterException e) {
			errorReporter.recordError(FeedbackGenerator.class, e);
		}
	}
	
	public void record( Reporting<T> reporting ) throws ReporterException {
		
		aggregateTriggerBank.record( reporting );
		
		try {
			periodTriggerBank.incr( reporting );
		} catch (OperationFailedException e) {
			errorReporter.recordError(FeedbackGenerator.class, e);
		}
		
		try {
			feedbackReceiver.reportItr( reporting );
		} catch (ReporterException e) {
			errorReporter.recordError(FeedbackGenerator.class, e);
		}
	}
	
	public void end( OptimizationFeedbackEndParams<T> optStep ) {
		
		try {
			aggregateTriggerBank.end();
		} catch (AggregatorException e) {
			errorReporter.recordError(FeedbackGenerator.class, e);
		}
		try {
			feedbackReceiver.reportEnd( optStep );
		} catch (ReporterException e) {
			errorReporter.recordError(FeedbackGenerator.class, e);
		}
	}
}