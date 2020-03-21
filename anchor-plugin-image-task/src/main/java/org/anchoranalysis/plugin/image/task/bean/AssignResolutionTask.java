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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.name.value.NameValue;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenneConstant;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.chnl.ChnlFilter;
import org.anchoranalysis.image.io.chnl.ChnlGetter;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.namestyle.StringSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

public class AssignResolutionTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389423680042363560L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean rgb = false;
	
	@BeanField
	private boolean supressSeries = false;
	
	@BeanField @Optional
	private ChnlFilter chnlFilter = null;
	// END BEAN PROPERTIES

	private GeneratorSequenceNonIncrementalRerouterErrors<Stack> generatorSeq;
	
	public AssignResolutionTask() {
		super();
	}

	@Override
	public void startSeries(BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter) throws JobExecutionException {
		
		StackGenerator generator = new StackGenerator(false, "out");
	
		generatorSeq =
			new GeneratorSequenceNonIncrementalRerouterErrors<>(
				new GeneratorSequenceNonIncrementalWriter<>(
					outputManager.getDelegate(),
					"",
					// NOTE WE ARE NOT ASSIGNING A NAME TO THE OUTPUT
					new StringSuffixOutputNameStyle("","%s"),
					generator,
					true
				),
				errorReporter	
			);
		generatorSeq.setSuppressSubfolder(true);
		
		// TODO it would be nicer to reflect the real sequence type, than just using a set of indexes
		generatorSeq.start(new SetSequenceType(), -1);
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	private Stack createRGBStack( ChnlGetter chnlCollection, int t ) throws IncorrectImageSizeException, RasterIOException, OperationFailedException, GetOperationFailedException {
		
		Stack stackRearranged = new Stack();
		
		ProgressReporter progressReporter = ProgressReporterNull.get();
		
		if (chnlCollection.hasChnl("red")) {
			stackRearranged.addChnl( chnlCollection.getChnl("red",t, progressReporter) );
		} else {
			stackRearranged.addBlankChnl();
		}
		
		
		if (chnlCollection.hasChnl("green")) {
			stackRearranged.addChnl( chnlCollection.getChnl("green",t, progressReporter) );
		} else {
			stackRearranged.addBlankChnl();
		}
		
		if (chnlCollection.hasChnl("blue")) {
			stackRearranged.addChnl( chnlCollection.getChnl("blue",t, progressReporter) );
		} else {
			stackRearranged.addBlankChnl();
		}
		
		return stackRearranged;
	}
	
	private String calculateSeriesTimeStringSupressSeries( int s, int t, int sizeT ) {
		if (sizeT>1) {
			return String.format("%05d", t);
		} else {
			return "";
		}
	}
		
	private String calculateSeriesTimeString( int s, int t, int sizeT ) {
		
		if (supressSeries) {
			return calculateSeriesTimeStringSupressSeries(s, t, sizeT);
		} else {
		
			if (sizeT>1) {
				return String.format("%03d_%05d", s, t);
			} else {
				return String.format("%03d", s );
			}
		}
	}
	
	@Override
	public void doStack( NamedChnlsInput inputObject, int seriesIndex, int numSeries, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter, ExperimentExecutionArguments expArgs ) throws JobExecutionException {
		
		try {
			NamedChnlCollectionForSeries chnlCollection = inputObject.createChnlCollectionForSeries(seriesIndex, ProgressReporterNull.get() );	
			
			ChnlGetter chnlAfter = chnlCollection;
			
			if (chnlFilter!=null) {
				chnlFilter.init(
					chnlCollection,
					expArgs.getModelDirectory(),
					logErrorReporter,
					new RandomNumberGeneratorMersenneConstant()
				);
				chnlAfter = chnlFilter;
			}
			
			// We add a filter if we have one
						
			ProgressReporter progressReporter = ProgressReporterNull.get();
			
			for( int t=0; t<chnlCollection.sizeT(progressReporter); t++) {
			
				String seriesTimeString = calculateSeriesTimeString(seriesIndex,t, chnlCollection.sizeT(progressReporter));
			
				try {
					if (rgb) {
						Stack stack = createRGBStack(chnlAfter,t);
						generatorSeq.add(stack, seriesTimeString );
					} else {
	
						List<NameValue<Stack>> stackList = new ArrayList<>(); 
						for( String key : chnlCollection.chnlNames() ) {
							
							Stack stack = new Stack(
								chnlAfter.getChnl(
									key,
									t,
									progressReporter
								)
							); 
							
							String combined = String.format("%s_%s", seriesTimeString, key );
							stackList.add(
								new NameValue<>(combined,stack)
							);
							
						}
						
						for( NameValue<Stack> ni : stackList ) {
							generatorSeq.add( ni.getValue(), ni.getName() );
						}
					}
				} catch (GetOperationFailedException e) {
					logErrorReporter.getLogReporter().logFormatted("Filter rejected %s", seriesTimeString);
					logErrorReporter.getErrorReporter().recordError(AssignResolutionTask.class, e);
				}
			}
		} catch (RasterIOException | OperationFailedException | IncorrectImageSizeException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public void endSeries(BoundOutputManagerRouteErrors outputManager) throws JobExecutionException {
		generatorSeq.end();
	}

	public boolean isRgb() {
		return rgb;
	}


	public void setRgb(boolean rgb) {
		this.rgb = rgb;
	}

	public boolean isSupressSeries() {
		return supressSeries;
	}

	public void setSupressSeries(boolean supressSeries) {
		this.supressSeries = supressSeries;
	}

	public ChnlFilter getChnlFilter() {
		return chnlFilter;
	}

	public void setChnlFilter(ChnlFilter chnlFilter) {
		this.chnlFilter = chnlFilter;
	}
}
