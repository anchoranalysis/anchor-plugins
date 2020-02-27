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
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporterConsole;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.core.random.RandomNumberGeneratorMersenneConstant;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.chnl.converter.ChnlConverterBean;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.chnl.ChnlFilter;
import org.anchoranalysis.image.io.chnl.ChnlGetter;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.namestyle.StringSuffixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.chnl.conversionstyle.ChnlConversionStyle;
import org.anchoranalysis.plugin.image.task.chnl.convert.ChnlGetterForTimepoint;

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
	
	/** 
	 * To convert as RGB or independently or in another way
	 */
	@BeanField
	private ChnlConversionStyle chnlConversionStyle = null;
	
	@BeanField
	private boolean suppressSeries = false;
	
	@BeanField @Optional
	private ChnlFilter chnlFilter = null;
	
	@BeanField @Optional
	private ChnlConverterBean chnlConverter = null;
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
	public void doStack( NamedChnlsInput inputObjectUntyped, int seriesIndex, int numSeries, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter, ExperimentExecutionArguments expArgs ) throws JobExecutionException {
		
		try {
			NamedChnlCollectionForSeries chnlCollection = createChnlCollection( inputObjectUntyped, seriesIndex );	
			
			ChnlGetter chnlGetter = maybeAddFilter(chnlCollection, logErrorReporter);

			if (chnlConverter!=null) {
				chnlGetter = maybeAddConverter(chnlGetter);
			}
			
			convertEachTimepoint(
				seriesIndex,
				chnlCollection.chnlNames(),
				numSeries,
				chnlCollection.sizeT(ProgressReporterNull.get()),
				chnlGetter,
				logErrorReporter
			);
						
		} catch (RasterIOException | CreateException | AnchorIOException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void convertEachTimepoint( int seriesIndex, Set<String> chnlNames, int numSeries, int sizeT, ChnlGetter chnlGetter, LogErrorReporter logErrorReporter ) throws AnchorIOException {
		
		for( int t=0; t<sizeT; t++) {
		
			CalcOutputName calcOutputName = new CalcOutputName(seriesIndex, numSeries, t, sizeT, suppressSeries);
			
			logErrorReporter.getLogReporter().logFormatted("Starting time-point: %d", t);
			
			ChnlGetterForTimepoint getterForTimepoint = new ChnlGetterForTimepoint(chnlGetter, t);
			
			chnlConversionStyle.convert(
				chnlNames,
				getterForTimepoint,
				(name,stack) -> addStackToOutput(name, stack, calcOutputName),
				logErrorReporter
			);
			
			logErrorReporter.getLogReporter().logFormatted("Ending time-point: %d", t);
		}
		
	}
	
	private void addStackToOutput(String name, Stack stack, CalcOutputName calcOutputName) {
		generatorSeq.add(
			stack,
			calcOutputName.calcOutputName(name)
		);
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

	public ChnlConversionStyle getChnlConversionStyle() {
		return chnlConversionStyle;
	}

	public void setChnlConversionStyle(ChnlConversionStyle chnlConversionStyle) {
		this.chnlConversionStyle = chnlConversionStyle;
	}
}
