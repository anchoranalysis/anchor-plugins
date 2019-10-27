package ch.ethz.biol.cell.mpp.feedback.reporter;

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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.ethz.biol.cell.mpp.feedback.Aggregator;
import ch.ethz.biol.cell.mpp.feedback.IAggregateReceiver;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackInitParams;
import ch.ethz.biol.cell.mpp.feedback.ReporterAgg;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;

public class Log4JReporter extends ReporterAgg<CfgNRGPixelized> implements IAggregateReceiver<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2915592624158759356L;
	private static Log log = LogFactory.getLog(Log4JReporter.class);
	
	// Constructor
	public Log4JReporter() {
		super();
	}
	
	@Override
	protected IAggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
		return this;
	}
	
	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) {
		
		String out = String.format(
			"*** itr=%d  size=%d  best_nrg=%e  kernel=%s",
			reporting.getIter(),
			reporting.getCfgNRGAfter().getCfg().size(),
			reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
			reporting.getKernel().getDescription()
		);
		log.info( out );
	}

	@Override
	public void aggStart( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg ) {
		
	}
	
	@Override
	public void aggReport( Reporting<CfgNRGPixelized> reporting, Aggregator agg ) {
		String out = String.format("itr=%d  %s", reporting.getIter(), agg.toString() );
		log.info( out );
	}
	
	@Override
	public void aggEnd( Aggregator agg ) {
		
	}

}
