package org.anchoranalysis.plugin.image.task.bean.feature;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalExceptional;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
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

	// START BEAN
	/**
	 * If non-null this file-path is used to determine the group of the file
	 * If null, the filename is used
	 */
	@BeanField @OptionalBean
	private FilePathGenerator groupGenerator;	// Translates an input file name to its group
		
	@BeanField @OptionalBean
	private FilePathGenerator idGenerator;	// Translates an input file name to a unique ID
	
	/** Features applied to each group to aggregate values (takes FeatureResultsVectorCollection) */
	@BeanField @OptionalBean
	private List<NamedBean<FeatureListProvider<FeatureInputResults>>> listFeaturesAggregate = new ArrayList<>();
	// END BEAN
	
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	/** Determines the unique image name from an inputPath */
	protected String extractImageIdentifier( Path inputPath, boolean debugMode ) throws AnchorIOException {
		return filePathAsIdentifier(idGenerator, inputPath, debugMode, path->path );
	}
	
	/** Determines the group name corresponding to an inputPath */
	protected String extractGroupName( Path inputPath, boolean debugMode ) throws AnchorIOException {
		return filePathAsIdentifier(groupGenerator, inputPath, debugMode, path->path.getFileName() );
	}
	
	
	@Override
	public void afterAllJobsAreExecuted(
			S sharedState,
			BoundIOContext context
	) throws ExperimentExecutionException {
		
		try {
			Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate = OptionalExceptional.map(
				Optional.ofNullable(listFeaturesAggregate),
				list-> NamedFeatureStoreFactory.createNamedFeatureList(list)
			);
			
			sharedState.writeFeaturesAsCSVForAllGroups(featuresAggregate, context);
		} catch (AnchorIOException | CreateException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	private static String filePathAsIdentifier( FilePathGenerator generator, Path path, boolean debugMode, Function<Path,Path> alternative ) throws AnchorIOException {
		Path out = determinePath(generator, path, debugMode, alternative);
		return FilePathToUnixStyleConverter.toStringUnixStyle(out);
	}
	
	private static Path determinePath( FilePathGenerator generator, Path path, boolean debugMode, Function<Path,Path> alternative ) throws AnchorIOException {
		if (generator!=null) {
			return generator.outFilePath(path, debugMode );
		} else {
			return alternative.apply(path);
		}
	}
	
	public FilePathGenerator getGroupGenerator() {
		return groupGenerator;
	}

	public void setGroupGenerator(FilePathGenerator groupGenerator) {
		this.groupGenerator = groupGenerator;
	}
	
	public FilePathGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(FilePathGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
	

	public List<NamedBean<FeatureListProvider<FeatureInputResults>>> getListFeaturesAggregate() {
		return listFeaturesAggregate;
	}

	public void setListFeaturesAggregate(
			List<NamedBean<FeatureListProvider<FeatureInputResults>>> listFeaturesAggregate) {
		this.listFeaturesAggregate = listFeaturesAggregate;
	}
}
