package org.anchoranalysis.plugin.io.bean.input.stack;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.stack.TimeSequence;

@FunctionalInterface
public interface TimeSequenceSupplier {

    TimeSequence get(ProgressReporter progressReporter) throws OperationFailedException;
}
