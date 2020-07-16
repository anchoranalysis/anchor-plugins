/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input;

import java.util.List;
import java.util.ListIterator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Limits the number of input-objects to a certain hard-maximum
 *
 * <p>If there's more input-objects than the maximum, the first maxNumItems are chosen
 *
 * @author Owen Feehan
 * @param <T>
 */
public class Limit<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;

    @BeanField private int maxNumItems = 0;
    // END BEAN PROPERTIES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        int i = 0;

        List<T> list = input.inputObjects(params);

        ListIterator<T> itr = list.listIterator();
        while (itr.hasNext()) {
            if (i++ >= maxNumItems) {
                itr.remove();
            }
        }
        return list;
    }

    public InputManager<T> getInput() {
        return input;
    }

    public void setInput(InputManager<T> input) {
        this.input = input;
    }

    public int getMaxNumItems() {
        return maxNumItems;
    }

    public void setMaxNumItems(int maxNumItems) {
        this.maxNumItems = maxNumItems;
    }
}
