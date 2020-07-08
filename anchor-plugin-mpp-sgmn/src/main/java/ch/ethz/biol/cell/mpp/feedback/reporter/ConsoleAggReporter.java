package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;

/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.AggregatorException;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.IAggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ConsoleAggReporter extends ReporterAgg<CfgNRGPixelized> implements IAggregateReceiver<CfgNRGPixelized> {
	
	private StopWatch timer = null;
	
	public ConsoleAggReporter(double aggIntervalLog10) {
		super(aggIntervalLog10);
	}
	
	@Override
	public void aggStart( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg ) throws AggregatorException {
		// NOTHING TO DO
	}
	
	@Override
	public void reportBegin( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams ) throws ReporterException {
		super.reportBegin(initParams);
		timer = new StopWatch();
		timer.start();
	}

	@Override
	public void aggReport( Reporting<CfgNRGPixelized> reporting, Aggregator agg ) {
		System.out.printf(		// NOSONAR
			"itr=%d  time=%e  tpi=%e   %s%n",
			reporting.getIter(),
			((double) timer.getTime()) / 1000,
			((double) timer.getTime()) / (reporting.getIter()*1000),
			agg.toString()
		);
	}
	
	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) throws ReporterException {
		System.out.printf(
			"*** itr=%d  size=%d  best_nrg=%e  kernel=%s%n",
			reporting.getIter(),
			reporting.getCfgNRGAfter().getCfg().size(),
			reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
			reporting.getKernel().getDescription()
		);
	}
	
	@Override
	public void aggEnd( Aggregator agg ) {
		// NOTHING TO DO
	}
	
	@Override
	protected IAggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
		return this;
	}

	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		super.reportEnd( optStep );
		timer.stop();
		System.out.printf( "Optimization time took %e s%n", ((double) timer.getTime()) / 1000 );	// NOSONAR
	}
}
