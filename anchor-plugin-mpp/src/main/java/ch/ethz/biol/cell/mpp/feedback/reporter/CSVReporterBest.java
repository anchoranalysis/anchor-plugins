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

import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackEndParams;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackInitParams;
import ch.ethz.biol.cell.mpp.feedback.ReporterOptimizationStep;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.feedback.ReporterException;

public class CSVReporterBest extends ReporterOptimizationStep<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8675901189777706521L;
	
	private FileOutput csvOutput;
	
	// Constructor
	public CSVReporterBest() {
		super();
	}
	
	@Override
	public void reportItr( Reporting<CfgNRGPixelized> reporting ) {

	}
	
	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) {
		if (csvOutput != null && csvOutput.isEnabled()) {

			this.csvOutput.getWriter().printf(
				"%d,%d,%e%n",
				reporting.getIter(),
				reporting.getCfgNRGAfter().getCfg().size(),
				reporting.getCfgNRGAfter().getCfgNRG().getNrgTotal()
			);
		}
	}
	

	@Override
	public void reportBegin( OptimizationFeedbackInitParams<CfgNRGPixelized> initParams ) throws ReporterException {

		try {
			// This can return NULL if the output isn't allowed
			this.csvOutput = CSVReporterUtilities.createFileOutputFor("csvStatsBest", initParams, "event_aggregate_stats"); 
			
			if (csvOutput!=null) {
				this.csvOutput.start();
				
				if (csvOutput.isEnabled()) {
					this.csvOutput.getWriter().printf("Itr,Size,Best_Nrg%n");
				}
			}
		} catch (IOException e) {
			throw new ReporterException(e);
		}

		
	}

	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		if (csvOutput != null && csvOutput.isEnabled()) {
			this.csvOutput.getWriter().close();
		}
	}
}
