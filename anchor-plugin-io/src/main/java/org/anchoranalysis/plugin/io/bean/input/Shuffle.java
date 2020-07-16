/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input;

import java.util.Collections;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Randomly shuffles the order of an input-manager
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class Shuffle<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;
    // END BEAN PROPERITES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        List<T> list = input.inputObjects(params);
        Collections.shuffle(list);
        return list;
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }
}
