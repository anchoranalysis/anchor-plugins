package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.cfg.ColoredCfg;
import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRGPixelized;
import org.anchoranalysis.anchor.mpp.mark.IDGetterMarkID;
import org.anchoranalysis.anchor.mpp.probmap.ProbMap;
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
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.OptionalOperationUnsupportedException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.objmask.writer.RGBOutlineWriter;
import org.anchoranalysis.io.color.HashedColorSet;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.manifest.sequencetype.ChangeSequenceType;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.namestyle.IntegerSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.cfg.ColoredCfgWithDisplayStack;
import org.anchoranalysis.mpp.io.cfg.generator.CfgGenerator;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterOptimizationStep;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;


public class CfgOnProbMapChangeReporter extends ReporterOptimizationStep<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7205394305776410851L;
	
	// START BEAN PARAMETERS
	@BeanField
	private String manifestFunction = "probMapSeries";
	
	@BeanField
	private String probMapID;
	
	@BeanField
	private String outputName;
	// END BEAN PARAMETERS
	
	private int numColors = 20;
	
	private BoundOutputManagerRouteErrors outputManager;
	
	private GeneratorSequenceNonIncrementalWriter<ColoredCfgWithDisplayStack> sequenceWriter;
	
	private ChangeSequenceType sequenceType;
	
	private Reporting<CfgNRGPixelized> lastOptimizationStep;
	
	private CfgGenerator iterableRaster;

	private ColorIndex colorIndex;
	
	//private static Log log = LogFactory.getLog(CfgOnProbMapChangeReporter.class);
	
	public CfgOnProbMapChangeReporter() {
		super();
	}
	
	// We generate an OutputName class from the outputName string
	private IndexableOutputNameStyle generateOutputNameStyle() {
		return new IntegerSuffixOutputNameStyle(outputName,"_%010d");
	}
	
	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		
		//IterableGenerator<CfgNRG> iterableGenerator = new ObjectOutputStreamGenerator<CfgNRG>("cfgNRG");
		
		sequenceType = new ChangeSequenceType();
		
	
		try {
			colorIndex = new HashedColorSet( new ShuffleColorSetGenerator( new HSBColorSetGenerator() ), numColors );
		} catch (OperationFailedException e1) {
			throw new ReporterException(e1);
		}
		
		// former background = initParams.getStack()
		iterableRaster = new CfgGenerator( new RGBOutlineWriter(), new IDGetterOverlayID() );
		
		
		//IterableGenerator<ProbMap> iterableGenerator = new ProbMapGenerator();
		
		IndexableOutputNameStyle outputNameStyle = generateOutputNameStyle();
		sequenceWriter = new GeneratorSequenceNonIncrementalWriter<>(
			outputManager.getDelegate(),
			outputNameStyle.getOutputName(),
			generateOutputNameStyle(),
			iterableRaster,
			true,
			new ManifestDescription("raster", manifestFunction)
		);
		
		try {
			sequenceWriter.start(sequenceType, -1);
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
	}

	@Override
	public void reportItr(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
		
		if (sequenceWriter!=null && reporting.isAccptd()) {
			
			// We convert the probMap into a raster, and update the background each time on our generator
			
			// TODO
			// It would be nicer if we actually sent the state through the main iterable interface to the generator
			//   but this will do for now, as a hack
			try {
				ProbMap probMap = getSharedObjects().getProbMapSet().getException(probMapID);
				Chnl chnl = probMap.visualization().getChnl();
				
				DisplayStack stack = DisplayStack.create( chnl );
				
				ColoredCfg coloredCfg = new ColoredCfg(
					reporting.getCfgNRGAfter().getCfg(),
					colorIndex,
					new IDGetterMarkID()
				);
				
				ColoredCfgWithDisplayStack cws = new ColoredCfgWithDisplayStack(coloredCfg, stack);
				
				sequenceWriter.add(
					cws,
					String.valueOf(reporting.getIter())
				);
				
			} catch (OutputWriteFailedException | OptionalOperationUnsupportedException | CreateException e) {
				throw new ReporterException(e);
			} catch (NamedProviderGetException e) {
				throw new ReporterException(e.summarize());
			}
		}
		lastOptimizationStep = reporting;
	}

	@Override
	public void reportEnd(OptimizationFeedbackEndParams<CfgNRGPixelized> optStep) throws ReporterException {
		
		if (sequenceWriter==null) {
			return;
		}
		
		if (sequenceWriter.isOn()) {
			if (lastOptimizationStep!=null) {
				sequenceType.setMaximumIndex(
					lastOptimizationStep.getIter()
				);
			}
		}
		
		try {
			sequenceWriter.end();
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
	}	
	
	public String getManifestFunction() {
		return manifestFunction;
	}

	public void setManifestFunction(String manifestFunction) {
		this.manifestFunction = manifestFunction;
	}

	public String getOutputName() {
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
		
	}

	public String getProbMapID() {
		return probMapID;
	}

	public void setProbMapID(String probMapID) {
		this.probMapID = probMapID;
	}
}