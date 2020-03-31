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
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.memory.MemoryUtilities;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterAgg;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.Aggregator;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.aggregate.IAggregateReceiver;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class MemoryUsageReporter extends ReporterAgg<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7100271659547754714L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean showBest = true;
	
	@BeanField
	private boolean showAgg = true;
	// END BEAN PROPERTIES

	private LogReporter logReporter;
	
	@Override
	protected IAggregateReceiver<CfgNRGPixelized> getAggregateReceiver() {
		return new IAggregateReceiver<CfgNRGPixelized>() {
			
			@Override
			public void aggStart(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams, Aggregator agg) {
				logReporter = initParams.getInitContext().getLogger().getLogReporter();
				MemoryUtilities.logMemoryUsage( "MemoryUsageReporter step=start", logReporter );
				
			}
			
			@Override
			public void aggReport(Reporting<CfgNRGPixelized> reporting, Aggregator agg) {
				
				if (!showAgg) {
					return;
				}
				
				MemoryUtilities.logMemoryUsage(
					String.format("MemoryUsageReporter AGG step=%d",reporting.getIter()	),
					logReporter
				);
			}
			
			@Override
			public void aggEnd(Aggregator agg) {
				MemoryUtilities.logMemoryUsage( "MemoryUsageReporter step=end", logReporter );
			}
		};
	}

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
		
		if (!showBest) {
			return;
		}
		
		MemoryUtilities.logMemoryUsage(
			String.format("MemoryUsageReporter BEST step=%d", reporting.getIter() ),
			logReporter
		);
	}

	public boolean isShowBest() {
		return showBest;
	}

	public void setShowBest(boolean showBest) {
		this.showBest = showBest;
	}

	public boolean isShowAgg() {
		return showAgg;
	}

	public void setShowAgg(boolean showAgg) {
		this.showAgg = showAgg;
	}
}
