/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.filter;

import java.util.List;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.io.input.filter.FilterDescriptiveNameEqualsContains;

/**
 * Filters all the input objects for only those with certain types of descriptive-names.
 *
 * <p>Either or both <i>equals</i> or <i>contains</i> conditions are possible
 *
 * @author Owen Feehan
 * @param <T> input-type
 */
public class FilterDescriptiveName<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;

    /**
     * A descriptive-name must be exactly equal to (case-sensitive) this string. If empty, disabled.
     */
    @BeanField @AllowEmpty private String equals = "";

    /** A descriptive-name must contain (case-sensitive) this string. If empty, disabled. */
    @BeanField @AllowEmpty private String contains = "";
    // END BEAN PROPERTIES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        FilterDescriptiveNameEqualsContains filter =
                new FilterDescriptiveNameEqualsContains(equals, contains);

        return filter.removeNonMatching(
                input.inputObjects(params) // Existing collection
                );
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }

    public String getEquals() {
        return equals;
    }

    public void setEquals(String equals) {
        this.equals = equals;
    }

    public String getContains() {
        return contains;
    }

    public void setContains(String contains) {
        this.contains = contains;
    }
}
