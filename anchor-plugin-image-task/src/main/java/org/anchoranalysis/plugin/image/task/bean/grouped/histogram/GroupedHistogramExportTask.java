package org.anchoranalysis.plugin.image.task.bean.grouped.histogram;



import java.io.IOException;

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


import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.core.name.CombinedName;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramArray;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedSharedState;
import org.anchoranalysis.plugin.image.task.bean.grouped.GroupedStackTask;
import org.anchoranalysis.plugin.image.task.grouped.ChnlSource;
import org.anchoranalysis.plugin.image.task.grouped.GroupMap;
import org.anchoranalysis.plugin.image.task.grouped.NamedChnl;


/** Calculates feature on a 'grouped' set of images
 
   1. All files are aggregated into groups
   2. For each image file, a histogram is calculated
   3. The histogram is added to the group histogram
   4. The histograms are written to the filesystem
   
**/
public class GroupedHistogramExportTask extends GroupedStackTask<Histogram,Histogram> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961126655531145104L;
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean writeImageHistograms = true;	// If enabled writes a histogram for each image, as well as the group
	
	@BeanField @Optional
	private ObjMaskProvider objMaskProvider;		// Optional
	
	/** If defined, this stack is used as a mask over the values which are fed into the histogram */
	@BeanField @AllowEmpty
	private String keyMask = "";
	
	/** What pixel value to read as "On" in the mask above */
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private boolean csvIgnoreZeros = false;
	// END BEAN PROPERTIES
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	@Override
	public GroupedSharedState<Histogram,Histogram> beforeAnyJobIsExecuted(
		BoundOutputManagerRouteErrors outputManager,
		ParametersExperiment params
	) throws ExperimentExecutionException {
		
		class HistogramGroupMap extends GroupMap<Histogram,Histogram> {
			
			public HistogramGroupMap(int maxValue) {
				super(
					"histogram",
					() -> new HistogramArray( maxValue ),
					(toAdd,group) -> group.addHistogram(toAdd)		
				);
			}
		}
		
		return new GroupedSharedState<Histogram,Histogram>(
			chnlChecker -> new HistogramGroupMap(
				(int) chnlChecker.getMaxValue() 
			)
		);
	}

	@Override
	protected void processKeys(
		NamedImgStackCollection store,
		String groupName,
		GroupedSharedState<Histogram,Histogram> sharedState,
		BoundOutputManagerRouteErrors outputManager,
		LogErrorReporter logErrorReporter
	) throws JobExecutionException {
		
		ChnlSource source = new ChnlSource( store, sharedState.getChnlChecker() );
		
		HistogramExtracter histogramExtracter = new HistogramExtracter(
			source,
			keyMask,
			maskValue
		);
			
		try {
			for( NamedChnl chnl : getSelectChnls().selectChnls(source, true)) {
				
				addHistogramFromChnl(
					chnl,
					histogramExtracter,
					groupName,
					sharedState.getGroupMap(),
					outputManager,
					logErrorReporter
				);
			}
			
		} catch (OperationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
	
	private void addHistogramFromChnl(
		NamedChnl chnl,
		HistogramExtracter histogramExtracter,
		String groupName,
		GroupMap<Histogram,Histogram> groupMap,
		BoundOutputManagerRouteErrors outputManager,
		LogErrorReporter logErrorReporter
	) throws JobExecutionException {
		
		Histogram hist = histogramExtracter.extractFrom(chnl.getChnl());
		
		if (writeImageHistograms) {
			// We keep histogram as private member variable so it is thread-safe
			try {
				createWriter().writeHistogramToFile(
					hist,
					chnl.getName(),
					chnl.getName(),
					outputManager,
					logErrorReporter
				);
			} catch (IOException e) {
				throw new JobExecutionException(e);
			}
		}
		
		groupMap.addToGroup(
			new CombinedName(groupName, chnl.getName() ),
			hist
		);
	}
	
	private GroupedHistogramWriter createWriter() {
		GroupedHistogramWriter writer = new GroupedHistogramWriter();
		writer.setIgnoreZeros(csvIgnoreZeros);
		return writer;
	}

		
	@Override
	public void afterAllJobsAreExecuted(
		BoundOutputManagerRouteErrors outputManager,
		GroupedSharedState<Histogram,Histogram> sharedState,
		LogReporter logReporter
	) throws ExperimentExecutionException {
		
		assert(logReporter!=null);
		LogErrorReporter logErrorReporter = new LogErrorReporter(logReporter);
		
		try {
			createWriter().writeAllGroupHistograms(
				sharedState.getGroupMap(),
				outputManager.resolveFolder("grouped"),
				logErrorReporter
			);
		} catch (IOException e) {
			throw new ExperimentExecutionException(e);
		}
	}

	public boolean isWriteImageHistograms() {
		return writeImageHistograms;
	}

	public void setWriteImageHistograms(boolean writeImageHistograms) {
		this.writeImageHistograms = writeImageHistograms;
	}

	public ObjMaskProvider getObjMaskProvider() {
		return objMaskProvider;
	}


	public void setObjMaskProvider(ObjMaskProvider objMaskProvider) {
		this.objMaskProvider = objMaskProvider;
	}

	public String getKeyMask() {
		return keyMask;
	}


	public void setKeyMask(String keyMask) {
		this.keyMask = keyMask;
	}


	public int getMaskValue() {
		return maskValue;
	}


	public void setMaskValue(int maskValue) {
		this.maskValue = maskValue;
	}

	public boolean isCsvIgnoreZeros() {
		return csvIgnoreZeros;
	}

	public void setCsvIgnoreZeros(boolean csvIgnoreZeros) {
		this.csvIgnoreZeros = csvIgnoreZeros;
	}

}
