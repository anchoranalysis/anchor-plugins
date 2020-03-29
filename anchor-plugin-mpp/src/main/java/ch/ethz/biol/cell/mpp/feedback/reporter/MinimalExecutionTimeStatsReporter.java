package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.generator.serialized.XStreamGenerator;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterOptimizationStep;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;
import org.apache.commons.lang.time.StopWatch;

public class MinimalExecutionTimeStatsReporter extends ReporterOptimizationStep<CfgNRGPixelized> {

	//private static Log log = LogFactory.getLog(MinimalExecutionTimeStatsReporter.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 920302426179556203L;

	// START BEAN PROPERTIES
	@BeanField
	private String outputName  = "minimalExecutionTimeStats";
	// END BEAN PROPERTIES
	
	
	private BoundOutputManagerRouteErrors outputManager = null;
	private KernelExecutionStats stats;
	private StopWatch stopWatch;

	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		outputManager = initParams.getInitContext().getOutputManager();
		stats = new KernelExecutionStats( initParams.getKernelFactoryList().size() );
		stopWatch = new StopWatch();
		stopWatch.start();
	}

	@Override
	public void reportItr(Reporting<CfgNRGPixelized> reporting) {
		
		int kernelID = reporting.getKernel().getID();
		double executionTime = reporting.getExecutionTime();
		
		if (reporting.hasProposal()) {
			
			if (reporting.isAccptd()) {
				stats.incrAccepted( kernelID, executionTime );
			} else {
				stats.incrRejected( kernelID, executionTime );
			}
		} else {
			// No proposal
			stats.incrNotProposed( kernelID, executionTime );
		}
	}

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
		
	}

	@Override
	public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) {
		stats.setTotalExecutionTime( stopWatch.getTime() );
		stopWatch.stop();
		
		outputManager.getWriterCheckIfAllowed().write(
			outputName,
			() -> new XStreamGenerator<>(stats, "minimalExecutionTimeStats")
		);
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}
}
