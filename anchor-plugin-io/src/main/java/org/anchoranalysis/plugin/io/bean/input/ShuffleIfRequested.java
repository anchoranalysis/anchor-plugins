package org.anchoranalysis.plugin.io.bean.input;

import java.util.Collections;
import java.util.List;
import org.anchoranalysis.io.input.InputContextParams;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.InputManagerUnary;

/**
 * Shuffles inputs if it is requested in the {@link InputContextParams} otherwise does not.
 * @author Owen Feehan
 *
 * @param <T> input-object type
 */
public class ShuffleIfRequested<T extends InputFromManager> extends InputManagerUnary<T> {

    @Override
    protected List<T> inputsFromDelegate(List<T> fromDelegate, InputManagerParams params)
            throws InputReadFailedException {

        if (params.getInputContext().isShuffle()) {
            Shuffle.shuffleInputs(fromDelegate, params);
        }
        return fromDelegate;
    }
}
