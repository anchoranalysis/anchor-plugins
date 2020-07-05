package org.anchoranalysis.plugin.image.task.bean.grouped;

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
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.plugin.image.task.bean.selectchnls.SelectAll;
import org.anchoranalysis.plugin.image.task.bean.selectchnls.SelectChnlsFromStacks;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;
import org.anchoranalysis.plugin.image.task.grouped.GroupedSharedState;

/**
 * Base class for stacks that somehow are grouped-together
 * 
 * @author Owen Feehan
 *
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
public abstract class GroupedStackTask<S,T> extends Task<ProvidesStackInput,GroupedSharedState<S,T>> {

	// START BEAN PROPERTIES
	/** If defined, translates a file-path into a group. If not-defined, all images are treated as part of the same group */
	@BeanField @OptionalBean
	private FilePathGenerator group;	
	
	@BeanField
	private SelectChnlsFromStacks selectChnls = new SelectAll();
	// END BEAN PROPERTIES

	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(ProvidesStackInput.class);
	}
		
	@Override
	public boolean hasVeryQuickPerInputExecution() {
		return false;
	}
	
	@Override
	public GroupedSharedState<S,T> beforeAnyJobIsExecuted(
		BoundOutputManagerRouteErrors outputManager,
		ParametersExperiment params
	) throws ExperimentExecutionException {
		return new GroupedSharedState<S,T>(
			chnlChecker -> createGroupMap(chnlChecker)
		);
	}
		
	@Override
	public void doJobOnInputObject(	InputBound<ProvidesStackInput,GroupedSharedState<S,T>> params) throws JobExecutionException {
		
		ProvidesStackInput inputObject = params.getInputObject();
		BoundIOContext context = params.context();

		// Extract a group name
		Optional<String> groupName = extractGroupName(
			inputObject.pathForBinding(),
			context.isDebugEnabled()
		);
		
		NamedImgStackCollection store = GroupedStackTask.extractInputStacks(inputObject);
	
		processKeys(
			store,
			groupName,
			params.getSharedState(),
			context
		);
	}
	
	@Override
	public void afterAllJobsAreExecuted(
		GroupedSharedState<S,T> sharedState,
		BoundIOContext context
	) throws ExperimentExecutionException {
		
		try {
			sharedState.getGroupMap().outputGroupedData(
				sharedState.getChnlChecker(),
				context.maybeSubdirectory(subdirectoryForGroupOutputs())
			);
	
		} catch (IOException e) {
			throw new ExperimentExecutionException(e);
		}
	}
	
	/** An optional sub-directory where the group outputs are placed, to avoid placing them in the top-level output */
	protected abstract Optional<String> subdirectoryForGroupOutputs();
	
	protected abstract GroupMapByName<S,T> createGroupMap( ConsistentChannelChecker chnlChecker );
	
	protected abstract void processKeys(
		NamedImgStackCollection store,
		Optional<String> groupName,
		GroupedSharedState<S,T> sharedState,
		BoundIOContext context
	) throws JobExecutionException;
	
	private Optional<String> extractGroupName( Optional<Path> path, boolean debugEnabled ) throws JobExecutionException {

		// 	Return an arbitrary group-name if there's no binding-path, or a group-generator is not defined		
		if (group==null || !path.isPresent()) {
			return Optional.empty();
		}
		
		try {
			return Optional.of(
				group.outFilePath( path.get(), debugEnabled ).toString()
			);
		} catch (AnchorIOException e) {
			throw new JobExecutionException(
				String.format("Cannot establish a group-identifier for: %s", path ),
				e
			);
		}
	}

	private static NamedImgStackCollection extractInputStacks( ProvidesStackInput inputObject ) throws JobExecutionException {
		NamedImgStackCollection stackCollection = new NamedImgStackCollection();
		try {
			inputObject.addToStore(
				new WrapStackAsTimeSequenceStore(stackCollection),
				0,
				ProgressReporterNull.get()
			);
		} catch (OperationFailedException e1) {
			throw new JobExecutionException("An error occurred creating inputs to the task",e1	);
		}
		return stackCollection;
	}

	public SelectChnlsFromStacks getSelectChnls() {
		return selectChnls;
	}

	public void setSelectChnls(SelectChnlsFromStacks selectChnls) {
		this.selectChnls = selectChnls;
	}

	public FilePathGenerator getGroup() {
		return group;
	}

	public void setGroup(FilePathGenerator group) {
		this.group = group;
	}
}
