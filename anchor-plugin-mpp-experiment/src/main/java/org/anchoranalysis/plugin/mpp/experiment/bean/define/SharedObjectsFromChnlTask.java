package org.anchoranalysis.plugin.mpp.experiment.bean.define;

import java.util.Optional;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
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
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.mpp.io.input.MPPInitParamsFactory;
import org.anchoranalysis.plugin.mpp.experiment.outputter.SharedObjectsUtilities;

public class SharedObjectsFromChnlTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	// START BEAN PROPERTIES
	@BeanField
	private Define define;
	
	@BeanField
	private String outputNameOriginal = "original";
	
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
	public void doStack(
		NamedChnlsInput inputObject,
		int seriesIndex,
		int numSeries,
		BoundIOContext context
	) throws JobExecutionException {

		NamedChnlCollectionForSeries ncc;
		try {
			ncc = inputObject.createChnlCollectionForSeries(0, ProgressReporterNull.get() );
		} catch (RasterIOException e1) {
			throw new JobExecutionException(e1);
		}
		
		try {
			Chnl inputImage = ncc.getChnlOrNull(ImgStackIdentifiers.INPUT_IMAGE, 0, ProgressReporterNull.get());
			if (inputImage!=null) {
				context.getOutputManager().getWriterCheckIfAllowed().write(
					outputNameOriginal,
					() -> new ChnlGenerator(inputImage,"original")
				);
			}

			MPPInitParams soMPP = MPPInitParamsFactory.create(
				context,
				Optional.ofNullable(define)
			);
			
			ncc.addAsSeparateChnls(
				new WrapStackAsTimeSequenceStore( soMPP.getImage().getStackCollection() ),
				0
			);
			
			if (suppressOutputExceptions) {
				SharedObjectsUtilities.output(soMPP, suppressSubfolders, context);
			} else {
				SharedObjectsUtilities.outputWithException(soMPP, context.getOutputManager(), suppressSubfolders);
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

	public Define getDefine() {
		return define;
	}

	public void setDefine(Define define) {
		this.define = define;
	}
}
