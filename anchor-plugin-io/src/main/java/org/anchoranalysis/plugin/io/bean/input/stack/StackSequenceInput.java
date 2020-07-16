/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.stack;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.progress.OperationWithProgressReporter;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.ProvidesStackInput;
import org.anchoranalysis.image.stack.TimeSequence;

/**
 * Provides a single stack (or a time series of such stacks) as an input
 *
 * @author Owen Feehan
 */
public abstract class StackSequenceInput extends ProvidesStackInput {

    // Creates a TimeSequence of ImgStack for a particular series number
    public abstract OperationWithProgressReporter<TimeSequence, OperationFailedException>
            createStackSequenceForSeries(int seriesNum) throws RasterIOException;
}
