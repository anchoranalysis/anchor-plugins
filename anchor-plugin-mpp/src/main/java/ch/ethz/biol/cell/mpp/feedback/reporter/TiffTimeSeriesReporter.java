package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.feature.bean.nrgscheme.NRGScheme;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.overlay.id.IDGetterOverlayID;

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
import org.anchoranalysis.core.bridge.IObjectBridge;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.idgetter.IDGetterIter;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.color.HashedColorSet;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.PeriodicSubfolderReporter;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

public class TiffTimeSeriesReporter extends PeriodicSubfolderReporter<CfgNRG> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6781381220576592268L;

	// START Bean Properties
	@BeanField
	private int numColors = 20;
	
	@BeanField
	private NRGScheme extraFeatures = new NRGScheme();
	// END Bean Properties
	
	private ColorIndex colorIndex;
	
	private static class CfgNRGToCfgBridge implements IObjectBridge<CfgNRG, ColoredCfgWithDisplayStack,Exception> {

		private DisplayStack stack;
		private ColorIndex colorIndex;
		
		public CfgNRGToCfgBridge(DisplayStack stack, ColorIndex colorIndex) {
			super();
			this.stack = stack;
			this.colorIndex = colorIndex;
		}

		@Override
		public ColoredCfgWithDisplayStack bridgeElement(CfgNRG sourceObject) {
			
			ColoredCfg coloredCfg = new ColoredCfg(sourceObject.getCfg(), colorIndex, new IDGetterIter<Mark>() );
			return new ColoredCfgWithDisplayStack( coloredCfg, stack );
		}
	}
	
	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
	
		try {
			colorIndex = new HashedColorSet( new ShuffleColorSetGenerator( new HSBColorSetGenerator() ), numColors );
		} catch (OperationFailedException e1) {
			throw new ReporterException(e1);
		}
		
		IterableGenerator<ColoredCfgWithDisplayStack> iterableRaster = new CfgGenerator( new RGBOutlineWriter(), new IDGetterOverlayID() );
		
		// This no longer needs to be combined, it's a legacy of when a HTML reporter was attached
		//   cleaning up woould be nice
		IterableCombinedListGenerator<CfgNRG> iterableCombined = new IterableCombinedListGenerator<>();
		iterableCombined.add(
				null,
				new IterableGeneratorBridge<>(
					iterableRaster,
					new CfgNRGToCfgBridge(
						initParams.getInitContext().getDualStack().getBgStack(),
						colorIndex
					)
				)
		);

		
		try {
			init( iterableCombined );
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
		
		super.reportBegin(initParams);
	}
	
	
	
	

	@Override
	protected CfgNRG generateIterableElement( Reporting<CfgNRGPixelized> reporting ) {
		return reporting.getCfgNRGAfter().getCfgNRG();
	}
	
	public TiffTimeSeriesReporter() {
		super();
	}

	public int getNumColors() {
		return numColors;
	}


	public void setNumColors(int numColors) {
		this.numColors = numColors;
	}


	public NRGScheme getExtraFeatures() {
		return extraFeatures;
	}


	public void setExtraFeatures(NRGScheme extraFeatures) {
		this.extraFeatures = extraFeatures;
	}

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
	}
}
