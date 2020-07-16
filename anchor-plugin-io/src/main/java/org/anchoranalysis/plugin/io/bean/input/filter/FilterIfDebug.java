/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.filter;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.params.DebugModeParams;
import org.anchoranalysis.plugin.io.input.filter.FilterDescriptiveNameEqualsContains;

/**
 * Filters a list of inputs when in debug-mode
 *
 * @author Owen Feehan
 * @param <T> input-type
 */
public class FilterIfDebug<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;
    // END BEAN PROPERTIES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        List<T> unfiltered = input.inputObjects(params);

        return params.getDebugModeParams()
                .map(dir -> takeFirst(maybeFilteredList(unfiltered, dir)))
                .orElse(unfiltered);
    }

    /** Take first item, if there's more than one in a list */
    private List<T> takeFirst(List<T> list) {
        if (list.size() <= 1) {
            // Nothing to do
            return list;
        } else {
            return Arrays.asList(list.get(0));
        }
    }

    private List<T> maybeFilteredList(List<T> unfiltered, DebugModeParams debugModeParams) {
        FilterDescriptiveNameEqualsContains filter =
                new FilterDescriptiveNameEqualsContains(
                        "", // Always disabled
                        debugModeParams.containsOrEmpty());

        return filter.removeNonMatching(unfiltered);
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }
}
