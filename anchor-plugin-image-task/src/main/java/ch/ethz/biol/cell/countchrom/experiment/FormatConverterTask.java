package ch.ethz.biol.cell.countchrom.experiment;

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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
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
 * 
 * @author Owen Feehan
 */
public class FormatConverterTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389423680042363560L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean rgb = false;
	
	@BeanField
	private boolean suppressSeries = false;
	
	@BeanField @Optional
	private ChnlFilter chnlFilter = null;
	
	@BeanField @Optional
	private ChnlConverterBean chnlConverter;
	
	// If TRUE and we cannot find a channel in the file, we ignore it and carry on
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
	
	public NamedChnlCollectionForSeries createChnlCollection(NamedChnlsInput inputObject, int seriesIndex) throws RasterIOException {
		 return inputObject.createChnlCollectionForSeries(seriesIndex, new ProgressReporterConsole(1) );
	}
	
	@Override
	public void doStack( NamedChnlsInput inputObjectUntyped, int seriesIndex, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter, String stackDescriptor, ExperimentExecutionArguments expArgs ) throws JobExecutionException {
		
		try {
			NamedChnlCollectionForSeries chnlCollection = createChnlCollection( inputObjectUntyped, seriesIndex );	
			
			ChnlGetter chnlAfter = chnlCollection;
			
			if (chnlFilter!=null) {
				
				chnlFilter.init(
					(NamedChnlCollectionForSeries) chnlCollection,
					logErrorReporter,
					new RandomNumberGeneratorMersenneConstant()
				);
				chnlAfter = chnlFilter;
			}
			if (chnlConverter!=null) {
				chnlAfter = new ConvertingChnlCollection(chnlAfter, chnlConverter.createConverter(), ConversionPolicy.CHANGE_EXISTING_CHANNEL);
			}
			
			
			// We add a filter if we have one
						
			ProgressReporter progressReporter = ProgressReporterNull.get();
			
			for( int t=0; t<chnlCollection.sizeT(progressReporter); t++) {
			
				logErrorReporter.getLogReporter().logFormatted("Starting time-point: %d", t);
				
				String seriesTimeString = calculateSeriesTimeString(seriesIndex,t, chnlCollection.sizeT(progressReporter));
			
				try {
					if (rgb) {
						Stack stack = createRGBStack(chnlAfter,t);
						generatorSeq.add(stack, seriesTimeString );
					} else {
	
						List<INameValue<Stack>> stackList = new ArrayList<>(); 
						for( String key : chnlCollection.chnlNames() ) {
							
							String combined;
							if (!seriesTimeString.isEmpty()) {
								combined = String.format("%s_%s", seriesTimeString, key );
							} else {
								combined = String.format("%s", key );
							}
							
							try {
								Chnl chnlConverted = chnlAfter.getChnl(key, t, progressReporter);
								stackList.add( new NameValue<>(combined, new Stack( chnlConverted )) );
							} catch (RasterIOException e) {
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
				} catch (GetOperationFailedException e) {
					logErrorReporter.getLogReporter().logFormatted("Filter rejected %s", seriesTimeString);
					logErrorReporter.getErrorReporter().recordError(FormatConverterTask.class, e);
				}
				
				logErrorReporter.getLogReporter().logFormatted("Ending time-point: %d", t);
				//outputManager.write("converted", new StackGenerator(true, "converted", factory) );
				//outputManager.writeStackAsComposite("converted", stackRearranged);
			}
		} catch (RasterIOException | OperationFailedException | IncorrectImageSizeException | CreateException e) {
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

}
