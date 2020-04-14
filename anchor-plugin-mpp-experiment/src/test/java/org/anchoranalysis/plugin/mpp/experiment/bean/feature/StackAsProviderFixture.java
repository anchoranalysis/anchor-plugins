package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
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
	public void close(ErrorReporter errorReporter) {
	
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