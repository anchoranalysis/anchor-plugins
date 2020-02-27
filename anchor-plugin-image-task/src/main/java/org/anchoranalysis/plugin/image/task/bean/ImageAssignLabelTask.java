package org.anchoranalysis.plugin.image.task.bean;



/*-
 * #%L
 * anchor-plugin-image-task
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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.init.ImageInitParams;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.io.input.StackInputInitParamsCreator;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.labeller.ImageLabeller;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateFilteredImageOutput;

/**
 * Assigns a label to each image and optionally
 *   1. copies each image into directory corresponding to the label (e.g. "positive", "negative)
 *   2. creates a single CSV file where each row is an image-label correspondence
 * 
 * @author FEEHANO
 *
 * @param T type of init-params associated with the filter
 */
public class ImageAssignLabelTask<T> extends Task<ProvidesStackInput,SharedStateFilteredImageOutput<T>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** Maps a label to an image */
	@BeanField
	private ImageLabeller<T> imageLabeller;
	
	/**
	 * If it's set, a stack is generated that is outputted into sub-directory corresponding to the groupIdentifier.
	 */
	@BeanField @Optional @SkipInit
	private StackProvider outputStackProvider;
	// END BEAN PROPERTIES
	
	public ImageAssignLabelTask() {
		super();
	}
	
	@Override
	public SharedStateFilteredImageOutput<T> beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		try {
			return new SharedStateFilteredImageOutput<>(
				outputManager,
				imageLabeller
			);
		} catch (CreateException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	protected void doJobOnInputObject(ParametersBound<ProvidesStackInput, SharedStateFilteredImageOutput<T>> params)
			throws JobExecutionException {
		
		try {
			String groupIdentifier = params.getSharedState().labelFor(
				params.getInputObject(),
				params.getLogErrorReporter()
			);
			
			params.getSharedState().writeRow(
				params.getInputObject().descriptiveName(),
				groupIdentifier
			);
			
			if (outputStackProvider!=null) {
				outputStack(
					groupIdentifier,
					StackInputInitParamsCreator.createInitParams(params.getInputObject(), params.getLogErrorReporter()),
					params.getInputObject().descriptiveName(),
					params.getSharedState(),
					params.getLogErrorReporter()
				);
			}
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	@Override
	public void afterAllJobsAreExecuted(BoundOutputManagerRouteErrors outputManager, SharedStateFilteredImageOutput<T> sharedState,
			LogReporter logReporter) throws ExperimentExecutionException {
		sharedState.close();
	}
	
	public StackProvider getOutputStackProvider() {
		return outputStackProvider;
	}


	public void setOutputStackProvider(StackProvider outputStackProvider) {
		this.outputStackProvider = outputStackProvider;
	}
	
	public ImageLabeller<T> getImageLabeller() {
		return imageLabeller;
	}

	public void setImageLabeller(ImageLabeller<T> imageLabeller) {
		this.imageLabeller = imageLabeller;
	}
	

	private void outputStack( String groupIdentifier, ImageInitParams initParams, String outputName, SharedStateFilteredImageOutput<T> sharedState, LogErrorReporter logErrorReporter ) throws JobExecutionException {

		try {
			outputStackProvider.initRecursive(initParams, logErrorReporter );

			BoundOutputManagerRouteErrors outputSub = sharedState.getOutputManagerFor(groupIdentifier);
			
			Stack stack = outputStackProvider.create();
		
			// Copies the file into the output
			outputSub.getWriterAlwaysAllowed().write(
				outputName,
				() -> new StackGenerator( stack, true, "raster" )
			);
		} catch (InitException | CreateException e) {
			throw new JobExecutionException(e);
		}
		
	}
}
