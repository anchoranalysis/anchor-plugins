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

import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.OutputWriteFailedException;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.io.output.file.FileOutputFromManager;
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

public final class TextFileReporter extends ReporterAgg<CfgNRGPixelized> implements IAggregateReceiver<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -232064240608574647L;

	private FileOutput fileOutput;
	
	private StopWatch timer = null;

	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) {
		if (fileOutput.isEnabled()) {
			
			fileOutput.getWriter().printf(
				"*** itr=%d  size=%d  best_nrg=%e  kernel=%s%n",
				reporting.getIter(),
				reporting.getCfgNRGAfter().getCfg().size(),
				reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal(),
				reporting.getKernel().getDescription()
			);
		}
	}
	
	@Override
	public void aggStart( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg ) throws AggregatorException {
		try {
			fileOutput = FileOutputFromManager.create(
				"txt",
				new ManifestDescription("text","event_log"),
				initParams.getInitContext().getOutputManager().getDelegate(),
				"eventLog"
			);
		} catch (OutputWriteFailedException e) {
			throw new AggregatorException(e);
		}
	}

	@Override
	public void aggReport( Reporting<CfgNRGPixelized> reporting, Aggregator agg ) {
		if (fileOutput.isEnabled()) {
			
			fileOutput.getWriter().printf(
				"itr=%d  time=%e  tpi=%e   %s%n",
				reporting.getIter(),
				((double) timer.getTime()) / 1000,
				((double) timer.getTime()) / (reporting.getIter()*1000),
				agg.toString()
			);
		}
	}
	
	@Override
	public void aggEnd( Aggregator agg ) {
		
	}
	
	@Override
	protected IAggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
		return this;
	}
	
	@Override
	public void reportBegin( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams ) throws ReporterException {
		
		super.reportBegin( initParams );
		try {
			fileOutput.start();
			
			if (fileOutput.isEnabled()) {
				timer = new StopWatch();
				timer.start();
			}
			
		} catch (IOException e) {
			throw new ReporterException(e);
		}
		
		
	}

	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		
		super.reportEnd( optStep );
		if (fileOutput.isEnabled()) {
			
			timer.stop();
			
			this.fileOutput.getWriter().print( optStep.toString() );
			this.fileOutput.getWriter().printf( "Optimization time took %e s%n", ((double) timer.getTime()) / 1000 );
			
			this.fileOutput.getWriter().close();
		}
	}

	public String getFilePath() {
		return fileOutput.getFilePath();
	}

	public void setFilePath(String filePath) {
		this.fileOutput.setFilePath(filePath);
	}
}
