package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.IAggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

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


import org.apache.commons.lang.time.StopWatch;

public final class OptimizationStepLogReporter extends ReporterAgg<CfgNRGPixelized> implements IAggregateReceiver<CfgNRGPixelized> {

	// START BEANS
	
	// END BEANS
	
	private StopWatch timer = null;
	
	private LogReporter logReporter;
	
	// Constructor
	public OptimizationStepLogReporter() {
		super();
	}
	
	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) throws ReporterException {
		
		logReporter.logFormatted(
			"*** itr=%d  size=%d  best_nrg=%e  kernel=%s",
			reporting.getIter(),
			reporting.getCfgNRGAfter().getCfg().size(),
			reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
			reporting.getKernel().getDescription()
		);
	}
	
	@Override
	public void aggReport( Reporting<CfgNRGPixelized> reporting, Aggregator agg ) {

		logReporter.logFormatted(
			"itr=%d  time=%e  tpi=%e  %s",
			reporting.getIter(),
			((double) timer.getTime()) / 1000,
			((double) timer.getTime()) / ( reporting.getIter()*1000),
			agg.toString()
		);
	}
	
	@Override
	public void aggEnd( Aggregator agg ) {
		
	}


	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		
		timer.stop();
		
		optStep.getLogReporter().log( optStep.getState().toString() );
		optStep.getLogReporter().logFormatted("Optimization time took %e s%n", ((double) timer.getTime()) / 1000);
		
		super.reportEnd( optStep );
	}

	
	@Override
	protected IAggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
		return this;
	}
	
	
	@Override
	public void reportBegin( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams ) throws ReporterException {
		
		super.reportBegin( initParams );
		
		timer = new StopWatch();
		timer.start();
		
		logReporter = initParams.getInitContext().getLogger().getLogReporter();
	}
	
	@Override
	public void aggStart( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg ) {
		
	}
}
