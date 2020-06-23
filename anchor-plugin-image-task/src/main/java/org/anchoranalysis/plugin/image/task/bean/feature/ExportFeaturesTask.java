package org.anchoranalysis.plugin.image.task.bean.feature;

import java.io.IOException;

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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.sharedstate.SharedStateExportFeatures;

/**
 * Base class for tasks that calculate features and export them as a CSV
 * 
 * @author FEEHANO
 *
 * @param <T> See Task
 * @param <S> See Task
 */
public abstract class ExportFeaturesTask<T extends InputFromManager, S extends SharedStateExportFeatures> extends Task<T,S> {

	private static final NamedFeatureStoreFactory STORE_FACTORY_AGGREGATE = NamedFeatureStoreFactory.bothNameAndParams();
	
	// START BEAN
	/**
	 * If non-null this file-path is used to determine the group of the file
	 * If null, the filename is used
	 */
	@BeanField @OptionalBean
	private FilePathGenerator group;	// Translates an input file name to its group
		
	@BeanField @OptionalBean
	private FilePathGenerator id;	// Translates an input file name to a unique ID
	
	/** Features applied to each group to aggregate values (takes FeatureResultsVectorCollection) */
	@BeanField @OptionalBean
	private List<NamedBean<FeatureListProvider<FeatureInputResults>>> listFeaturesAggregate;
	// END BEAN
	
	@Override
	public S beforeAnyJobIsExecuted(BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
			throws ExperimentExecutionException {
		try {
			return createSharedState(
				createMetadataHeaders(),
				params.context()
			);
		} catch (CreateException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	public void doJobOnInputObject(InputBound<T,S> input) throws JobExecutionException {
		try {
			Optional<String> groupName = extractGroupNameFromGenerator(
				input.getInputObject().pathForBindingRequired(),
				input.context().isDebugEnabled()
			);
			calcAllResultsForInput(input, groupName);
		} catch (OperationFailedException | AnchorIOException e) {
			throw new JobExecutionException(e);
		}
	}
		
	@Override
	public void afterAllJobsAreExecuted(
			S sharedState,
			BoundIOContext context
	) throws ExperimentExecutionException {
		
		try {
			sharedState.closeAnyOpenIO();
			
			Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate = OptionalUtilities.map(
				Optional.ofNullable(listFeaturesAggregate),
				STORE_FACTORY_AGGREGATE::createNamedFeatureList
			);
			
			sharedState.writeGroupedResults(
				featuresAggregate,
				includeGroupInExperiment( isGroupGeneratorDefined() ),
				context
			);
		} catch (AnchorIOException | CreateException | IOException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}

	/** Determines the unique image name from an inputPath */
	protected String extractImageIdentifier( Path inputPath, boolean debugMode ) throws AnchorIOException {
		return filePathAsIdentifier(
			Optional.ofNullable(id),
			inputPath,
			debugMode,
			path-> FilePathToUnixStyleConverter.toStringUnixStyle(path)
		);
	}
	
	protected abstract S createSharedState( MetadataHeaders metadataHeaders, BoundIOContext context) throws CreateException;
	
	/** Iff true, group columns are added to the CSV exports, and other group exports may occur in sub-directories 
	 * @param groupGeneratorDefined TODO*/
	protected abstract boolean includeGroupInExperiment(boolean groupGeneratorDefined);
	
	protected abstract String[] headersForResults();
	
	protected abstract String[] headersForGroup(boolean groupGeneratorDefined);

	protected abstract void calcAllResultsForInput(InputBound<T,S> input, Optional<String> groupGeneratorName) throws OperationFailedException;
	
	private MetadataHeaders createMetadataHeaders() {
		return new MetadataHeaders(
			headersForGroup( isGroupGeneratorDefined() ),
			headersForResults()
		);
	}

	private boolean isGroupGeneratorDefined() {
		return group!=null;
	}
	
	private static Optional<String> filePathAsIdentifier(
		Optional<FilePathGenerator> generator,
		Path path,
		boolean debugMode
	) throws AnchorIOException {
		return OptionalUtilities.map(
			generator,
			gen-> FilePathToUnixStyleConverter.toStringUnixStyle(
				gen.outFilePath(path, debugMode)
			)
		);
	}
	
	private static String filePathAsIdentifier( Optional<FilePathGenerator> generator, Path path, boolean debugMode, Function<Path,String> alternative ) throws AnchorIOException {
		return filePathAsIdentifier(generator, path, debugMode).orElseGet( ()->
			alternative.apply(path)
		);
	}
	
	/** Determines the group name corresponding to an inputPath and the group-generator */
	private Optional<String> extractGroupNameFromGenerator(Path inputPath, boolean debugMode) throws AnchorIOException {
		return filePathAsIdentifier(
			Optional.ofNullable(group),
			inputPath,
			debugMode
		);
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInputResults>>> getListFeaturesAggregate() {
		return listFeaturesAggregate;
	}

	public void setListFeaturesAggregate(
			List<NamedBean<FeatureListProvider<FeatureInputResults>>> listFeaturesAggregate) {
		this.listFeaturesAggregate = listFeaturesAggregate;
	}

	public FilePathGenerator getGroup() {
		return group;
	}

	public void setGroup(FilePathGenerator group) {
		this.group = group;
	}

	public FilePathGenerator getId() {
		return id;
	}

	public void setId(FilePathGenerator id) {
		this.id = id;
	}
}
