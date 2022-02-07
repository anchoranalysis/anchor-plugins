package org.anchoranalysis.plugin.io.bean.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputsWithDirectory;

/**
 * Helps sort inputs.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class SortHelper {

    /**
     * Sorts the inputs that come from a delegate.
     *
     * @param <T> input-type
     * @param fromDelegate the delegate that supplies the inputs.
     * @return a newly created {@link InputsWithDirectory} with identical directory, but with sorted
     *     inputs.
     */
    public static <T extends InputFromManager> InputsWithDirectory<T> sortInputs(
            InputsWithDirectory<T> fromDelegate) {
        List<T> list = new ArrayList<>(fromDelegate.inputs());
        Collections.sort(list, (T o1, T o2) -> o1.identifier().compareTo(o2.identifier()));
        return fromDelegate.withInputs(list);
    }
}
