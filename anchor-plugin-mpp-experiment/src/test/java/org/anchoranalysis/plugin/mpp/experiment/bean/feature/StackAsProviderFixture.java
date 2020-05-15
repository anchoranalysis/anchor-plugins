package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import java.nio.file.Paths;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.name.store.NamedProviderStore;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;

class StackAsProviderFixture extends ProvidesStackInput {

	private Stack stack;
	private String name;
	
	public StackAsProviderFixture(Stack stack, String name) {
		super();
		this.stack = stack;
		this.name = name;
	}

	@Override
	public String descriptiveName() {
		return "arbitraryName";
	}

	@Override
	public Path pathForBinding() {
		return Paths.get("arbitraryPath/");
	}

	@Override
	public void addToStore(NamedProviderStore<TimeSequence> stackCollection, int seriesNum,
			ProgressReporter progressReporter) throws OperationFailedException {
		addToStoreWithName(name, stackCollection, 0, progressReporter);
	}

	@Override
	public void addToStoreWithName(String name, NamedProviderStore<TimeSequence> stackCollection, int seriesNum,
			ProgressReporter progressReporter) throws OperationFailedException {
		stackCollection.add(
			name,
			() -> new TimeSequence(stack)
		);
	}

	@Override
	public int numFrames() throws OperationFailedException {
		return 1;
	}
}
