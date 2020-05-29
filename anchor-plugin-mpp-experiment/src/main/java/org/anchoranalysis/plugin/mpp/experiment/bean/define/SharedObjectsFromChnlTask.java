package org.anchoranalysis.plugin.mpp.experiment.bean.define;



import java.util.Optional;

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
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;
import org.anchoranalysis.io.output.bound.BoundIOContext;

public class SharedObjectsFromChnlTask extends RasterTask {

	// START BEAN PROPERTIES
	@BeanField
	private DefineOutputterMPP define;
	
	@BeanField
	private String outputNameOriginal = "original";
	// END BEAN PROPERTIES

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
			Optional<Chnl> inputImage = ncc.getChnlOrNull(ImgStackIdentifiers.INPUT_IMAGE, 0, ProgressReporterNull.get());
			inputImage.ifPresent( image ->
				context.getOutputManager().getWriterCheckIfAllowed().write(
					outputNameOriginal,
					() -> new ChnlGenerator(image,"original")
				)
			);

			define.processInput(ncc, context);
						
		} catch (GetOperationFailedException | OperationFailedException e) {
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

	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	public DefineOutputterMPP getDefine() {
		return define;
	}

	public void setDefine(DefineOutputterMPP define) {
		this.define = define;
	}

}
