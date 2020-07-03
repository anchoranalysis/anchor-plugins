package ch.ethz.biol.cell.mpp.feedback.reporter;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.core.functional.OptionalUtilities;

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
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.FeedbackReceiverBean;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class CSVReporterBest extends FeedbackReceiverBean<CfgNRGPixelized> {

	private Optional<FileOutput> csvOutput;
	
	@Override
	public void reportItr( Reporting<CfgNRGPixelized> reporting ) {
		// NOTHING TO DO
	}
	
	@Override
	public void reportNewBest( Reporting<CfgNRGPixelized> reporting ) throws ReporterException {
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
			this.csvOutput = createOutput(initParams);
			OptionalUtilities.ifPresent(
				csvOutput,
				output-> {
					output.start();
					output.getWriter().printf("Itr,Size,Best_Nrg%n");
				}
			);
		} catch (AnchorIOException e) {
			throw new ReporterException(e);
		}
	}

	@Override
	public void reportEnd( OptimizationFeedbackEndParams<CfgNRGPixelized> optStep ) {
		csvOutput.ifPresent( output->
			output.getWriter().close()
		);
	}
	
	private Optional<FileOutput> createOutput(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		try {
			return CSVReporterUtilities.createFileOutputFor("csvStatsBest", initParams, "event_aggregate_stats");
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		} 
	}
}
