/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Uses one input-manager normally, but a different one if in debug mode
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class BranchIfDebug<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;

    // If set to null and we are in debug mode, we take the first item from the normal input
    @BeanField @OptionalBean private InputManager<T> inputDebug;
    // END BEAN PROPERTIES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        if (params.isDebugModeActivated()) {
            if (inputDebug == null) {
                // We pick the first
                Iterator<T> all = input.inputObjects(params).iterator();
                T firstItem = all.next();
                return Collections.singletonList(firstItem);
            }

            return inputDebug.inputObjects(params);
        }
        return input.inputObjects(params);
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }

    public InputManager<T> getInputDebug() {
        return inputDebug;
    }

    public void setInputDebug(InputManager<T> inputDebug) {
        this.inputDebug = inputDebug;
    }
}
