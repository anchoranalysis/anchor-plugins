package org.anchoranalysis.plugin.io.bean.input;

import org.anchoranalysis.io.input.InputContextParams;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.InputManagerUnary;

/**
 * Shuffles inputs if it is requested in the {@link InputContextParams} otherwise does not.
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class ShuffleIfRequested<T extends InputFromManager> extends InputManagerUnary<T> {

    @Override
    protected InputsWithDirectory<T> inputsFromDelegate(
            InputsWithDirectory<T> fromDelegate, InputManagerParams params)
            throws InputReadFailedException {

        if (params.getInputContext().isShuffle()) {
            Shuffle.shuffleInputs(fromDelegate.inputs(), params);
        }
        return fromDelegate;
    }
}
