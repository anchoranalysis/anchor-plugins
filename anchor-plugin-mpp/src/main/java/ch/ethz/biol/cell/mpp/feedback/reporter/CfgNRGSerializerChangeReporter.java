package ch.ethz.biol.cell.mpp.feedback.reporter;

import org.anchoranalysis.anchor.mpp.feature.nrg.cfg.CfgNRG;
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
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.generator.serialized.BundledObjectOutputStreamGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.manifest.deserializer.bundle.BundleParameters;
import org.anchoranalysis.io.manifest.sequencetype.ChangeSequenceType;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.namestyle.IntegerSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.sgmn.bean.optscheme.feedback.ReporterOptimizationStep;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackEndParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.OptimizationFeedbackInitParams;
import org.anchoranalysis.mpp.sgmn.optscheme.feedback.ReporterException;
import org.anchoranalysis.mpp.sgmn.optscheme.step.Reporting;


public class CfgNRGSerializerChangeReporter extends ReporterOptimizationStep<CfgNRGPixelized> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1774424246128858626L;
	
	
	// START BEAN PARAMETERS
	@BeanField
	private String manifestFunction = "cfgNRG";
	
	@BeanField
	private String outputName;
	
	@BeanField
	private int bundleSize = 1000;
	
	@BeanField
	private boolean best = false;
	// END BEAN PARAMETERS
	
	private BoundOutputManagerRouteErrors outputManager;
	
	private GeneratorSequenceNonIncrementalWriter<CfgNRG> sequenceWriter;
	
	private ChangeSequenceType sequenceType;
	
	private Reporting<CfgNRGPixelized> lastOptimizationStep;
	
	//private static Log log = LogFactory.getLog(CfgNRGSerializerChangeReporter.class);
	
	public CfgNRGSerializerChangeReporter() {
		super();
	}
	
	// We generate an OutputName class from the outputName string
	private IndexableOutputNameStyle generateOutputNameStyle() {
		return new IntegerSuffixOutputNameStyle(outputName,10);
	}
	
	@Override
	public void reportBegin(OptimizationFeedbackInitParams<CfgNRGPixelized> initParams) throws ReporterException {
		
		//IterableGenerator<CfgNRG> iterableGenerator = new ObjectOutputStreamGenerator<CfgNRG>("cfgNRG");
		
		outputManager = initParams.getInitContext().getOutputManager();
		
		sequenceType = new ChangeSequenceType();
		
		BundleParameters bundleParams = new BundleParameters();
		bundleParams.setBundleSize(bundleSize);
		bundleParams.setSequenceType(sequenceType);
		
		IterableGenerator<CfgNRG> iterableGenerator = new BundledObjectOutputStreamGenerator<>(
			bundleParams,
			this.generateOutputNameStyle(),
			outputManager.getDelegate(), 
			manifestFunction
		);
		
		IndexableOutputNameStyle outputNameStyle = generateOutputNameStyle();
		sequenceWriter = new GeneratorSequenceNonIncrementalWriter<>(
			outputManager.getDelegate(),
			outputNameStyle.getOutputName(),			
			outputNameStyle,
			iterableGenerator,
			true,
			new ManifestDescription("serialized", manifestFunction)
		);
		
		try {
			sequenceWriter.start(sequenceType, -1);
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}

		
		//IterableGenerator<CfgNRG> iterableGenerator = new XStreamGenerator<CfgNRG>("cfgNRG");
		//init( new ObjectOutputStreamGenerator<CfgNRG>() );

		/*BundleParameters bundleParams = new BundleParameters();
		bundleParams.setBundleSize(10000);
		
		SequenceType sequenceType = init( new BundledObjectOutputStreamGenerator<CfgNRG>(
			bundleParams,
			this.generateOutputNameStyle(),
			getParentOutputManager(), 
			manifestFunction
		));
				
		bundleParams.setSequenceType( sequenceType );*/
	}

	@Override
	public void reportItr(Reporting<CfgNRGPixelized> reporting) throws ReporterException {
		
		try {
			if (reporting.isAccptd() && sequenceWriter!=null) {
				if (best) {
					
					if (reporting.getBest()!=null) {
					
						sequenceWriter.add(
							reporting.getBest().getCfgNRG(),
							String.valueOf(reporting.getIter())
						);
					}
				} else {
					
					if (reporting.getCfgNRGAfter()!=null) {
					
						sequenceWriter.add(
							reporting.getCfgNRGAfter().getCfgNRG(),
							String.valueOf(reporting.getIter())
						);
					}
				}
			}
			lastOptimizationStep = reporting;
		} catch (OutputWriteFailedException e) {
			throw new ReporterException(e);
		}
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

	public int getBundleSize() {
		return bundleSize;
	}

	public void setBundleSize(int bundleSize) {
		this.bundleSize = bundleSize;
	}

	public boolean isBest() {
		return best;
	}

	public void setBest(boolean best) {
		this.best = best;
	}

	@Override
	public void reportNewBest(Reporting<CfgNRGPixelized> reporting) {
		
	}
}