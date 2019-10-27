package ch.ethz.biol.cell.countchrom.experiment;

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
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionArguments;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.io.input.NamedChnlsInputAsStack;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalRerouterErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceNonIncrementalWriter;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.io.output.namestyle.StringSuffixOutputNameStyle;

public class MovieFromSlicesTask extends RasterTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389423680042363560L;

	// START BEAN PROPERTIES
	@BeanField
	private boolean rgb = false;
	
	@BeanField
	private int delaySizeAtEnd = 0;
	
	@BeanField
	private String filePrefix = "movie";
	
	@BeanField
	private int width = -1;
	
	@BeanField
	private int height = -1;
	
	@BeanField
	private int startIndex = 0;	// this does nothing atm
	
	@BeanField
	private int repeat = 1;
	// END BEAN PROPERTIES

	private int index = 0;
	private GeneratorSequenceNonIncrementalRerouterErrors<Stack> generatorSeq;
	
	public MovieFromSlicesTask() {
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

	private String formatIndex( int index ) {
		return String.format("%s_%08d",filePrefix, index);
	}
	
	@Override
	public void doStack( NamedChnlsInputAsStack inputObject, int seriesIndex, BoundOutputManagerRouteErrors outputManager, LogErrorReporter logErrorReporter, String stackDescriptor, ExperimentExecutionArguments expArgs ) throws JobExecutionException {
		
		try {
			NamedChnlCollectionForSeries ncc = inputObject.createChnlCollectionForSeries(0, ProgressReporterNull.get() );
			
			ProgressReporter progressReporter = ProgressReporterNull.get();
			
			Chnl red = ncc.getChnl("red", 0, progressReporter);
			Chnl blue = ncc.getChnl("blue", 0, progressReporter);
			Chnl green = ncc.getChnl("green", 0, progressReporter);
			
			//
			if (!red.getDimensions().equals(blue.getDimensions()) || !blue.getDimensions().equals(green.getDimensions()) ) {
				throw new JobExecutionException("Scene dimensions do not match");
			}
			
			Stack sliceOut = null;
			
			ExtractProjectedStack extract = new ExtractProjectedStack(width, height);
			
			for (int z=0; z<red.getDimensions().getZ(); z++) {
				
				sliceOut = extract.extractAndProjectStack(red, green, blue, z);
				
				for( int i=0; i<repeat; i++) {
					generatorSeq.add(sliceOut, formatIndex(index) );
					index++;
				}
			}
			
			// Just
			if (delaySizeAtEnd>0 && sliceOut!=null) {
				for (int i=0; i<delaySizeAtEnd; i++) {
					generatorSeq.add(sliceOut, formatIndex(index) );
					index++;					
				}
			}
			
			
		} catch (RasterIOException | IncorrectImageSizeException e) {
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

	public int getDelaySizeAtEnd() {
		return delaySizeAtEnd;
	}

	public void setDelaySizeAtEnd(int delaySizeAtEnd) {
		this.delaySizeAtEnd = delaySizeAtEnd;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
}
