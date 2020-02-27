package ch.ethz.biol.cell.countchrom.experiment;

import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;

/*
 * #%L
 * anchor-plugin-mpp-experiment
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
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorBean;
import org.anchoranalysis.bean.shared.random.RandomNumberGeneratorMersenneConstantBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.store.SharedObjects;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.experiment.identifiers.ImgStackIdentifiers;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.ChnlGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

public class SharedObjectsFromChnlTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// START BEAN PROPERTIES
	@BeanField
	private Define namedDefinitions;
	
	@BeanField
	private String outputNameOriginal = "original";
	
	@BeanField
	private RandomNumberGeneratorBean randomNumberGenerator = new RandomNumberGeneratorMersenneConstantBean();
	
	@BeanField
	private boolean suppressSubfolders = true;
	
	@BeanField
	private boolean suppressOutputExceptions = false;
	// END BEAN PROPERTIES

	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	@Override
	public void doStack(NamedChnlsInput inputObject,
			int seriesIndex, int numSeries,
			BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter, ExperimentExecutionArguments expArgs)
			throws JobExecutionException {

		NamedChnlCollectionForSeries ncc;
		try {
			ncc = inputObject.createChnlCollectionForSeries(0, ProgressReporterNull.get() );
		} catch (RasterIOException e1) {
			throw new JobExecutionException(e1);
		}
		
		try {
			Chnl inputImage = ncc.getChnlOrNull(ImgStackIdentifiers.INPUT_IMAGE, 0, ProgressReporterNull.get());
			if (inputImage!=null) {
				outputManager.getWriterCheckIfAllowed().write(
					outputNameOriginal,
					() -> new ChnlGenerator(inputImage,"original")
				);
			}
			
		
			SharedObjects so = new SharedObjects(logErrorReporter);
			MPPInitParams soMPP = MPPInitParams.create(so,namedDefinitions,logErrorReporter,randomNumberGenerator.create());
			
			ncc.addToStackCollection(
				new WrapStackAsTimeSequenceStore( soMPP.getImage().getStackCollection() ),
				0
			);
			
			if (suppressOutputExceptions) {
				SharedObjectsUtilities.output(soMPP, outputManager, logErrorReporter, suppressSubfolders);
			} else {
				SharedObjectsUtilities.outputWithException(soMPP, outputManager, suppressSubfolders);
			}
			
		} catch (GetOperationFailedException | OperationFailedException | OutputWriteFailedException | CreateException e) {
			throw new JobExecutionException(e);
		}
	}
	
	@Override
	public void startSeries(BoundOutputManagerRouteErrors outputManager,
			ErrorReporter errorReporter) throws JobExecutionException {
	}


	@Override
	public void endSeries(BoundOutputManagerRouteErrors outputManager)
			throws JobExecutionException {
	
	}
	
	public Define getNamedDefinitions() {
		return namedDefinitions;
	}


	public void setNamedDefinitions(Define namedDefinitions) {
		this.namedDefinitions = namedDefinitions;
	}

	public RandomNumberGeneratorBean getRandomNumberGenerator() {
		return randomNumberGenerator;
	}


	public void setRandomNumberGenerator(RandomNumberGeneratorBean randomNumberGenerator) {
		this.randomNumberGenerator = randomNumberGenerator;
	}


	public boolean isSuppressSubfolders() {
		return suppressSubfolders;
	}


	public void setSuppressSubfolders(boolean suppressSubfolders) {
		this.suppressSubfolders = suppressSubfolders;
	}


	public boolean isSuppressOutputExceptions() {
		return suppressOutputExceptions;
	}


	public void setSuppressOutputExceptions(boolean suppressOutputExceptions) {
		this.suppressOutputExceptions = suppressOutputExceptions;
	}
}
