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
import java.util.Set;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.value.INameValue;
import org.anchoranalysis.core.name.value.NameValue;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenneConstant;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.chnl.converter.ChnlConverterBean;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.chnl.ChnlFilter;
import org.anchoranalysis.image.io.chnl.ChnlGetter;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.namestyle.StringSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/**
 * Converts the input-image to the default output format, optionally changing the bit depth
 * 
 * <p>If it looks like an RGB image, channels are written together. Otherwise they are written independently</p>
 * 
 * @author Owen Feehan
 */
public class FormatConverterTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389423680042363560L;

	// START BEAN PROPERTIES
	
	/** If an image has exactly 3 channels, or less than 3 channels and one of them is called 'red' or 'blue' or 'green', then it is
	 *    outputted as a RGB color image, rather than 3 separate channels
	 */
	@BeanField
	private boolean preferRGB = true;
	
	@BeanField
	private boolean suppressSeries = false;
	
	@BeanField @Optional
	private ChnlFilter chnlFilter = null;
	
	@BeanField @Optional
	private ChnlConverterBean chnlConverter = null;
	
	/** Iff TRUE and we cannot find a channel in the file, we ignore it and carry on */
	@BeanField
	private boolean ignoreMissingChnl = false;
	// END BEAN PROPERTIES

	private GeneratorSequenceNonIncrementalRerouterErrors<Stack> generatorSeq;
	
	public FormatConverterTask() {
		super();
	}
	
	@Override
	public void startSeries(BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter) throws JobExecutionException {
		
		StackGenerator generator = new StackGenerator(false, "out" );
	
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
		
	public NamedChnlCollectionForSeries createChnlCollection(NamedChnlsInput inputObject, int seriesIndex) throws RasterIOException {
		 return inputObject.createChnlCollectionForSeries(seriesIndex, new ProgressReporterConsole(1) );
	}
	
	@Override
	public void doStack( NamedChnlsInput inputObjectUntyped, int seriesIndex, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter, String stackDescriptor, ExperimentExecutionArguments expArgs ) throws JobExecutionException {
		
		try {
			NamedChnlCollectionForSeries chnlCollection = createChnlCollection( inputObjectUntyped, seriesIndex );	
			
			ChnlGetter chnlGetter = maybeAddFilter(chnlCollection, logErrorReporter);

			if (chnlConverter!=null) {
				chnlGetter = maybeAddConverter(chnlGetter);
			}
			
			convertEachTimepoint(
				seriesIndex,
				chnlCollection.chnlNames(),
				chnlCollection.sizeT(ProgressReporterNull.get()),
				chnlGetter,
				logErrorReporter
			);
						
		} catch (RasterIOException | CreateException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void convertEachTimepoint( int seriesIndex, Set<String> chnlNames, int sizeT, ChnlGetter chnlGetter, LogErrorReporter logErrorReporter ) throws JobExecutionException {
		ProgressReporter progressReporter = ProgressReporterNull.get();
		
		for( int t=0; t<sizeT; t++) {
		
			logErrorReporter.getLogReporter().logFormatted("Starting time-point: %d", t);
			
			String seriesTimeString = calculateSeriesTimeString(seriesIndex,t, sizeT);
		
			if (preferRGB && chnlNamesAreRGB(chnlNames)) {
				convertRGBChnls(seriesTimeString, chnlGetter, t, logErrorReporter);
			} else {
				convertIndependentChnls(chnlNames, seriesTimeString, chnlGetter, t, progressReporter, logErrorReporter);
			}
			
			logErrorReporter.getLogReporter().logFormatted("Ending time-point: %d", t);
		}
		
	}
	
	private static boolean chnlNamesAreRGB(Set<String> chnlNames) {
		if (chnlNames.size()>3) {
			return false;
		}
		
		for( String key : chnlNames) {
			// If a key doesn't match one of the expected red-green-blue names
			if (!(key.equals("red") || key.equals("green") || key.equals("blue"))) {
				return false;
			}
		}
		
		return true;
	}
	
	private ChnlGetter maybeAddConverter( ChnlGetter chnlGetter ) throws CreateException {
		if (chnlConverter!=null) {
			return new ConvertingChnlCollection(chnlGetter, chnlConverter.createConverter(), ConversionPolicy.CHANGE_EXISTING_CHANNEL);
		} else {
			return chnlGetter;
		}
	}
	
	private ChnlGetter maybeAddFilter( NamedChnlCollectionForSeries chnlCollection, LogErrorReporter logErrorReporter ) {
		
		if (chnlFilter!=null) {
			
			chnlFilter.init(
				(NamedChnlCollectionForSeries) chnlCollection,
				logErrorReporter,
				new RandomNumberGeneratorMersenneConstant()
			);
			return chnlFilter;
		} else {
			return chnlCollection;
		}
	}
	
	private void convertRGBChnls(
		String seriesTimeString,
		ChnlGetter chnlAfter,
		int t,
		LogErrorReporter logErrorReporter
	) throws JobExecutionException {
		try {
			Stack stack = createRGBStack(chnlAfter, t, logErrorReporter.getLogReporter());
			generatorSeq.add(stack, seriesTimeString );
		} catch (CreateException e) {
			throw new JobExecutionException("Incorrect image size", e);
		}
	}
	
	private Stack createRGBStack( ChnlGetter chnlCollection, int t, LogReporter logReporter ) throws CreateException  {
		
		Stack stackRearranged = new Stack();
		
		addChnlOrBlank("red", chnlCollection, t, stackRearranged, logReporter);
		addChnlOrBlank("green", chnlCollection, t, stackRearranged, logReporter);
		addChnlOrBlank("blue", chnlCollection, t, stackRearranged, logReporter);
		
		return stackRearranged;
	}
	
	private void addChnlOrBlank( String chnlName, ChnlGetter chnlCollection, int t, Stack stackRearranged, LogReporter logReporter ) throws CreateException {
		try {
			if (chnlCollection.hasChnl(chnlName)) {
				stackRearranged.addChnl( chnlCollection.getChnl(chnlName,t, ProgressReporterNull.get()) );
			} else {
				logReporter.logFormatted(
					String.format("Adding a blank channel for %s for t=%d", chnlName, t)
				);
				stackRearranged.addBlankChnl();
			}
		} catch (IncorrectImageSizeException | OperationFailedException | GetOperationFailedException e) {
			throw new CreateException(e);
		}
	}
	
	private String calculateSeriesTimeStringSupressSeries( int s, int t, int sizeT ) {
		if (sizeT>1) {
			return String.format("%05d", t);
		} else {
			return "";
		}
	}
		
	private String calculateSeriesTimeString( int s, int t, int sizeT ) {
		
		if (suppressSeries) {
			return calculateSeriesTimeStringSupressSeries(s, t, sizeT);
		} else {
		
			if (sizeT>1) {
				return String.format("%03d_%05d", s, t);
			} else {
				return String.format("%03d", s );
			}
		}
	}
	
	private void convertIndependentChnls(
			Set<String> chnlNames,
			String seriesTimeString,
			ChnlGetter chnlAfter,
			int t,
			ProgressReporter progressReporter,
			LogErrorReporter logErrorReporter
		) throws JobExecutionException {
		
		List<INameValue<Stack>> stackList = new ArrayList<>(); 
		for( String key : chnlNames ) {
			
			String combined;
			if (!seriesTimeString.isEmpty()) {
				combined = String.format("%s_%s", seriesTimeString, key );
			} else {
				combined = String.format("%s", key );
			}
			
			try {
				Chnl chnlConverted = chnlAfter.getChnl(key, t, progressReporter);
				stackList.add( new NameValue<>(combined, new Stack( chnlConverted )) );
			} catch (GetOperationFailedException e) {
				if (ignoreMissingChnl) {
					logErrorReporter.getLogReporter().logFormatted("Cannot open channel '%s. Ignoring.", key);
					continue;
				} else {
					throw new JobExecutionException( String.format("Cannot open channel '%s'", key), e );
				}
			}
			
		}
		
		for( INameValue<Stack> ni : stackList ) {
			generatorSeq.add( ni.getValue(), ni.getName() );
		}
	}

	@Override
	public void endSeries(BoundOutputManagerRouteErrors outputManager) throws JobExecutionException {
		generatorSeq.end();
	}

	public boolean isSuppressSeries() {
		return suppressSeries;
	}

	public void setSuppressSeries(boolean supressSeries) {
		this.suppressSeries = supressSeries;
	}

	public ChnlFilter getChnlFilter() {
		return chnlFilter;
	}

	public void setChnlFilter(ChnlFilter chnlFilter) {
		this.chnlFilter = chnlFilter;
	}

	public ChnlConverterBean getChnlConverter() {
		return chnlConverter;
	}

	public void setChnlConverter(ChnlConverterBean chnlConverter) {
		this.chnlConverter = chnlConverter;
	}

	public boolean isIgnoreMissingChnl() {
		return ignoreMissingChnl;
	}

	public void setIgnoreMissingChnl(boolean ignoreMissingChnl) {
		this.ignoreMissingChnl = ignoreMissingChnl;
	}

	public boolean isPreferRGB() {
		return preferRGB;
	}

	public void setPreferRGB(boolean preferRGB) {
		this.preferRGB = preferRGB;
	}

}
