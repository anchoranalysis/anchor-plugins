package org.anchoranalysis.plugin.image.task.bean.grouped;

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
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.Task;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGeneratorConstant;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.bean.selectchnls.SelectAll;
import org.anchoranalysis.plugin.image.task.bean.selectchnls.SelectChnlsFromStacks;

/**
 * Base class for stacks that somehow are grouped-together
 * 
 * @author FEEHANO
 *
 * @param <S> individual-type
 * @param <T> aggregate-type
 */
public abstract class GroupedStackTask<S,T> extends Task<ProvidesStackInput,GroupedSharedState<S,T>> {

	// START BEAN PROPERTIES
	@BeanField
	private FilePathGenerator groupGenerator = new FilePathGeneratorConstant("all");	// Translates an input file name to it's group
	
	@BeanField
	private SelectChnlsFromStacks selectChnls = new SelectAll();
	// END BEAN PROPERTIES

	@Override
	public InputTypesExpected inputTypesExpected() {
		return new InputTypesExpected(ProvidesStackInput.class);
	}
	
	@Override
	public void doJobOnInputObject(	InputBound<ProvidesStackInput,GroupedSharedState<S,T>> params) throws JobExecutionException {
		
		ProvidesStackInput inputObject = params.getInputObject();
		BoundIOContext context = params.context();

		// Extract a group name
		String groupName = extractGroupName( inputObject.pathForBinding(), context.isDebugEnabled() );
		
		NamedImgStackCollection store = GroupedStackTask.extractInputStacks(inputObject);
	
		processKeys(
			store,
			groupName,
			params.getSharedState(),
			context
		);
	}
	
	protected abstract void processKeys(
		NamedImgStackCollection store,
		String groupName,
		GroupedSharedState<S,T> sharedState,
		BoundIOContext context
	) throws JobExecutionException;
	
	private String extractGroupName( Path path, boolean debugEnabled ) throws JobExecutionException {
		try {
			return groupGenerator.outFilePath( path, debugEnabled ).toString();
		} catch (AnchorIOException e1) {
			throw new JobExecutionException(
				String.format("Cannot establish a group-identifier for: %s", path ),
				e1
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
	
	public FilePathGenerator getGroupGenerator() {
		return groupGenerator;
	}

	public void setGroupGenerator(FilePathGenerator groupGenerator) {
		this.groupGenerator = groupGenerator;
	}

	public SelectChnlsFromStacks getSelectChnls() {
		return selectChnls;
	}

	public void setSelectChnls(SelectChnlsFromStacks selectChnls) {
		this.selectChnls = selectChnls;
	}

}
