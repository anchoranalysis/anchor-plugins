/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Sorts the input-items in alphabetical order of their descriptiveName()
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class SortAlphabetically<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;
    // END BEAN PROPERITES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        List<T> list = new ArrayList<>();

        Iterator<T> itr = input.inputObjects(params).iterator();
        while (itr.hasNext()) {
            list.add(itr.next());
        }

        Collections.sort(
                list, (T o1, T o2) -> o1.descriptiveName().compareTo(o2.descriptiveName()));

        return list;
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }
}
