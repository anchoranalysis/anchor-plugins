package org.anchoranalysis.plugin.io.bean.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** 
 * Helps sort inputs.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class SortHelper {

	/**
	 * Sorts the inputs that come from a delegate.
	 * 
	 * @param <T> input-type
	 * @param fromDelegate the delegate that supplies the inputs.
	 * @return a newly created {@link InputsWithDirectory} with identical directory, but with sorted inputs.
	 * @throws InputReadFailedException if called by {@link InputsWithDirectory#inputs}.
	 */
    public static <T extends InputFromManager> InputsWithDirectory<T> sortInputs(
            InputsWithDirectory<T> fromDelegate)
            throws InputReadFailedException {
        List<T> list = new ArrayList<>(fromDelegate.inputs());
        Collections.sort(list, (T o1, T o2) -> o1.identifier().compareTo(o2.identifier()));
        return fromDelegate.withInputs(list);
    }
}
