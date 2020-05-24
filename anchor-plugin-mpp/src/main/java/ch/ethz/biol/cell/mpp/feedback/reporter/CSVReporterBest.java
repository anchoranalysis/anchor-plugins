package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;

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

import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.file.FileOutput;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterOptimizationStep;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class CSVReporterBest extends ReporterOptimizationStep<CfgNRGPixelized> {

	private Optional<FileOutput> csvOutput;
	
	// Constructor
	public CSVReporterBest() {
		super();
	}
	
	@Override
	public void reportItr( Reporting<CfgNRGPixelized> reporting ) {

	}
	
	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) {
		if (csvOutput.isPresent() && csvOutput.get().isEnabled()) {

			this.csvOutput.get().getWriter().printf(
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
			try {
				this.csvOutput = CSVReporterUtilities.createFileOutputFor("csvStatsBest", initParams, "event_aggregate_stats");
			} catch (OutputWriteFailedException e) {
				throw new ReporterException(e);
			} 
			
			if (csvOutput.isPresent()) {
				this.csvOutput.get().start();
				this.csvOutput.get().getWriter().printf("Itr,Size,Best_Nrg%n");
			}
		} catch (AnchorIOException e) {
			throw new ReporterException(e);
		}

		
	}

	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		if (csvOutput.isPresent() && csvOutput.get().isEnabled()) {
			this.csvOutput.get().getWriter().close();
		}
	}
}
