package ch.ethz.biol.cell.mpp.feedback.reporter;

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


import java.io.IOException;

import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

import ch.ethz.biol.cell.mpp.feedback.Aggregator;
import ch.ethz.biol.cell.mpp.feedback.AggregatorException;
import ch.ethz.biol.cell.mpp.feedback.IAggregateReceiver;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackEndParams;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackInitParams;
import ch.ethz.biol.cell.mpp.feedback.ReporterAgg;
import ch.ethz.biol.cell.mpp.feedback.ReporterException;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;

public class CSVReporterAgg extends ReporterAgg<CfgNRGPixelized> implements IAggregateReceiver<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7829811659377706642L;

	private FileOutput csvOutput;
	
	private StopWatch timer = null;

	private long crntTime;
	
	private long lastTime;
	
	private long totalCount;
	
	// N.B.
	//  we can make this faster by only doing the aggregation if necessary
	//  we probably should separate the two types of csv writing, as they have effectively nothing in common

	// Constructor
	public CSVReporterAgg() {
		super();
	}

	@Override
	protected IAggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
		return this;
	}

	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		
		super.reportBegin( initParams );
		
		this.csvOutput = CSVReporterUtilities.createFileOutputFor("csvStatsAgg", initParams, "interval_aggregate_stats");
		
		timer = new StopWatch();
		timer.start();
		
		crntTime = 0;
		lastTime = 0;
		totalCount = 0;
		
		
	}

	@Override
	public void aggStart( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg ) throws AggregatorException {
		
		try {
			if (csvOutput!=null) {
				this.csvOutput.start();
				
				if (csvOutput.isEnabled()) {
					this.csvOutput.getWriter().print("Itr,");
					agg.outputHeaderToWriter(this.csvOutput.getWriter());
					this.csvOutput.getWriter().print(",Time,TimePerIter,IntervalTimePerIter");
					this.csvOutput.getWriter().println();
				}
			}
			
		} catch (IOException e) {
			throw new AggregatorException(e);
		}
	}
	
	@Override
	public void aggReport( Reporting<CfgNRGPixelized> reporting, Aggregator agg ) {
		if (csvOutput!=null && csvOutput.isEnabled()) {

			// Shift time
			this.lastTime = this.crntTime;
			this.crntTime = this.timer.getTime();
			
			totalCount++;
			long totalItr = getAggInterval() * totalCount;
			
			csvOutput.getWriter().printf("%d,", totalItr ); 
			
			agg.outputToWriter( csvOutput.getWriter() );
			
			csvOutput.getWriter().printf(",%e,%e,%e", toSeconds(this.crntTime), toSeconds(this.crntTime)/totalItr, toSeconds((this.crntTime-this.lastTime))/getAggInterval() );
			csvOutput.getWriter().println();
			csvOutput.getWriter().flush();
		}
	}
	
	@Override
	public void aggEnd( Aggregator agg ) {
		
	}
	
	private static double toSeconds( long time ) {
		return (time / 1000);
	}

	
	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		super.reportEnd( optStep );
		
		timer.stop();
		
		if (csvOutput!=null && csvOutput.isEnabled()) {
			this.csvOutput.end();
		}
		
	}


	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
	}


}
