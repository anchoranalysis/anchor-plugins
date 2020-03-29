package org.anchoranalysis.plugin.image.task.bean;

/*
 * #%L
 * anchor-plugin-image-task
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


import java.util.Set;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.provider.INamedProvider;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenneTime;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.io.stack.StackCollectionOutputter;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryImgChnlProviderScaleXY;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderScale;


/**
 * Scales many rasters
 * 
 * Expects a second-level output "stack" to determine which stacks get ouputted or not
 * 
 * @author Owen Feehan
 *
 */
public class ScaleTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String KEY_OUTPUT_STACK = "stack";
	
	// START BEAN PROPERTIES
	@BeanField
	private ScaleCalculator scaleCalculator;
	
	@BeanField
	private boolean forceBinary = false;
	// END BEAN PROPERTIES
	
	@Override
	public void startSeries(BoundOutputManagerRouteErrors outputManager,
			ErrorReporter errorReporter) throws JobExecutionException {
		
	}

	@Override
	public void doStack(NamedChnlsInput inputObject,
			int seriesIndex, int numSeries,
			BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter,
			ExperimentExecutionArguments expArgs) throws JobExecutionException {
	
		// Input
		NamedChnlCollectionForSeries nccfs;
		try {
			nccfs = inputObject.createChnlCollectionForSeries(0, ProgressReporterNull.get() );
		} catch (RasterIOException e1) {
			throw new JobExecutionException(e1);
		}
		

		
		
		
		// Our output collections
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
		NamedImgStackCollection stackCollectionMIP = new NamedImgStackCollection();
		

		ImageInitParams soImage = ImageInitParams.create(
			logErrorReporter,
			new RandomNumberGeneratorMersenneTime(),
			expArgs.getModelDirectory()
		);
		
		try {
			// We store each channel as a stack in our collection, in case they need to be referenced by the scale calculator
			nccfs.addAsSeparateChnls(
				new WrapStackAsTimeSequenceStore( soImage.getStackCollection() ),
				0
			);
			//scaleCalculator.initRecursive(soImage.getSharedObjects(), logErrorReporter);
			scaleCalculator.initRecursive(logErrorReporter);
		} catch (InitException | OperationFailedException e) {
			throw new JobExecutionException(e);
		}
		
		populateOutputCollectionsFromSharedObjects(
			soImage,
			stackCollection,
			stackCollectionMIP,
			outputManager,
			logErrorReporter
		);
		
		outputStackCollection( stackCollection, outputManager, KEY_OUTPUT_STACK, "chnlScaledCollection", logErrorReporter.getErrorReporter() );
		outputStackCollection( stackCollectionMIP, outputManager, KEY_OUTPUT_STACK, "chnlScaledCollectionMIP", logErrorReporter.getErrorReporter() );
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	private static void outputStackCollection(
		INamedProvider<Stack> stackCollection,
		BoundOutputManagerRouteErrors outputManager,
		String outputSecondLevelKey,
		String outputName,
		ErrorReporter errorReporter
	) {
		StackCollectionOutputter.output(
			StackCollectionOutputter.subset(
				stackCollection,
				outputManager.outputAllowedSecondLevel(outputSecondLevelKey)
			),
			outputManager.getDelegate(),
			outputName,
			"",
			errorReporter,
			false
		);
	}

	
	private void populateOutputCollectionsFromSharedObjects(
		ImageInitParams so,
		NamedImgStackCollection stackCollection,
		NamedImgStackCollection stackCollectionMIP,
		BoundOutputManagerRouteErrors outputManager,
		LogErrorReporter logErrorReporter
	) throws JobExecutionException {
				
		Set<String> chnlNames = so.getStackCollection().keys();
		for( String chnlName : chnlNames ) {
			
			// If this output is not allowed we simply skip
			if (!outputManager.outputAllowedSecondLevel(KEY_OUTPUT_STACK).isOutputAllowed(chnlName)) {
				continue;
			}
			
			try {
				Chnl chnlIn = so.getStackCollection().getException(chnlName).getChnl(0);
				
				Chnl chnlOut;
				if (forceBinary) {
					BinaryChnl binaryImg = new BinaryChnl(chnlIn, BinaryValues.getDefault() );
					chnlOut = BinaryImgChnlProviderScaleXY.scale(binaryImg, scaleCalculator, InterpolatorFactory.getInstance().binaryResizing()).getChnl();
				} else {
					chnlOut = ChnlProviderScale.scale(chnlIn, scaleCalculator, InterpolatorFactory.getInstance().rasterResizing(), logErrorReporter);
				}
				
				stackCollection.addImageStack(chnlName, new Stack(chnlOut) );
				stackCollectionMIP.addImageStack(chnlName, new Stack(chnlOut.maxIntensityProj()) );
				
			} catch (CreateException e) {
				throw new JobExecutionException(e);
			} catch (NamedProviderGetException e) {
				throw new JobExecutionException(e.summarize());
			}
		}
	}
	
	
	@Override
	public void endSeries(BoundOutputManagerRouteErrors outputManager)
			throws JobExecutionException {
	}

	public ScaleCalculator getScaleCalculator() {
		return scaleCalculator;
	}

	public void setScaleCalculator(ScaleCalculator scaleCalculator) {
		this.scaleCalculator = scaleCalculator;
	}

	public boolean isForceBinary() {
		return forceBinary;
	}

	public void setForceBinary(boolean forceBinary) {
		this.forceBinary = forceBinary;
	}

}
