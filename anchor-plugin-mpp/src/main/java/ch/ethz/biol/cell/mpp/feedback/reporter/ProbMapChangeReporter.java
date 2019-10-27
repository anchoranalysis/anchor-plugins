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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.name.provider.INamedProvider;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.manifest.sequencetype.ChangeSequenceType;
import org.anchoranalysis.io.output.OutputWriteFailedException;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.output.namestyle.IntegerSuffixOutputNameStyle;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;

import ch.ethz.biol.cell.imageprocessing.io.generator.raster.ProbMapGenerator;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackEndParams;
import ch.ethz.biol.cell.mpp.feedback.OptimizationFeedbackInitParams;
import ch.ethz.biol.cell.mpp.feedback.ReporterOptimizationStep;
import ch.ethz.biol.cell.mpp.nrg.CfgNRGPixelized;
import ch.ethz.biol.cell.mpp.feedback.ReporterException;
import ch.ethz.biol.cell.mpp.probmap.ProbMap;


// We index the ProbMap as the state BEFORE a given iteration
public class ProbMapChangeReporter extends ReporterOptimizationStep<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3841547442965467354L;
	
	// START BEAN PARAMETERS
	@BeanField
	private String manifestFunction = "probMapSeries";
	
	@BeanField
	private String probMapID;
	
	@BeanField
	private String outputName;
	// END BEAN PARAMETERS
	
	private BoundOutputManagerRouteErrors outputManager;
	
	private GeneratorSequenceNonIncrementalWriter<ProbMap> sequenceWriter;
	
	private ChangeSequenceType sequenceType;
	
	private Reporting<CfgNRGPixelized> lastOptimizationStep;
	
	//private static Log log = LogFactory.getLog(ProbMapChangeReporter.class);
	
	public ProbMapChangeReporter() {
		super();
	}
	
	// We generate an OutputName class from the outputName string
	private  IndexableOutputNameStyle generateOutputNameStyle() {
		return new IntegerSuffixOutputNameStyle(outputName,"_%010d");
	}
	
	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		
		//IterableGenerator<CfgNRG> iterableGenerator = new ObjectOutputStreamGenerator<CfgNRG>("cfgNRG");
		
		sequenceType = new ChangeSequenceType();
		
	
		IterableGenerator<ProbMap> iterableGenerator = new ProbMapGenerator();
		
		IndexableOutputNameStyle outputNameStyle = generateOutputNameStyle();
		sequenceWriter = new GeneratorSequenceNonIncrementalWriter<>(
			outputManager.getDelegate(),
			outputNameStyle.getOutputName(),
			outputNameStyle,
			iterableGenerator,
			true,
			new ManifestDescription("raster", manifestFunction)
		);
		
		try {
			sequenceWriter.start(sequenceType, -1);
			
			writeForIndex( getSharedObjects().getProbMapSet(), "0" );
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
	}
	
	private void writeForIndex( INamedProvider<ProbMap> namedProbMapSet, String indexOut ) throws ReporterException {
		try {
			ProbMap probMap = namedProbMapSet.getException(probMapID);
			
			// We always add one to the iteration, to show the ProbMap at the beginning
			sequenceWriter.add( probMap, indexOut );
			
		} catch (GetOperationFailedException e) {
			throw new ReporterException(e);
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
	}

	@Override
	public void reportItr(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
		
		if (reporting.isAccptd() && sequenceWriter!=null) {
			writeForIndex(
				getSharedObjects().getProbMapSet(),
				String.valueOf(reporting.getIter() + 1)
			);
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
				sequenceType.setMaximumIndex(lastOptimizationStep.getIter());
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