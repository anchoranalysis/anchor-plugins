package ch.ethz.biol.cell.mpp.feedback.reporter;

/*-
 * #%L
 * anchor-plugin-mpp
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.util.function.Function;

import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackEndParams;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackInitParams;
import ch.ethz.biol.cell.mpp.feedback.ReporterOptimizationStep;
import ch.ethz.biol.cell.mpp.nrg.CfgNRG;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.feedback.ReporterException;


public class ConsoleAcceptedReporter extends ReporterOptimizationStep<CfgNRGPixelized> {

	// START BEAN PARAMETERS
	// END BEAN PARAMETERS
	
	
	//private OptimizationStep lastOptimizationStep;
	
	//private static Log log = LogFactory.getLog(ConsoleAcceptedReporter.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6363971468964257234L;

	private LogErrorReporter logger;
	
	public ConsoleAcceptedReporter() {
		super();
	}
	
	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		this.logger = initParams.getInitContext().getLogger();
	}

	@Override
	public void reportItr(Reporting<CfgNRGPixelized> reporting) {
		
		if (reporting.isAccptd()) {
			
			logger.getLogReporter().logFormatted(
				"itr=%5d  size=%3d  nrg=%e  best_nrg=%e   kernel=%s",
				reporting.getIter(),
				extractStatInt(reporting.getCfgNRGAfter(), a->a.getCfg().size() ),
				extractStatDbl(reporting.getCfgNRGAfter(), a->a.getNrgTotal() ),
				extractStatDbl(reporting.getBest(), a->a.getNrgTotal() ),
				reporting.getKernel().getDescription()
			);
		}
		//lastOptimizationStep = optStep;
	}
	
	/* Only extract a stat if non-null */
	private static double extractStatDbl( CfgNRGPixelized cfgNRG, Function<CfgNRG,Double> func ) {
		if (cfgNRG!= null) {
			return func.apply( cfgNRG.getCfgNRG() );
		} else {
			return Double.NaN;
		}
	}
	
	private static int extractStatInt( CfgNRGPixelized cfgNRG, Function<CfgNRG,Integer> func ) {
		if (cfgNRG!= null) {
			return func.apply( cfgNRG.getCfgNRG() );
		} else {
			return Integer.MIN_VALUE;
		}
	}

	@Override
	public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {
		
	}	

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
		
	}

}
