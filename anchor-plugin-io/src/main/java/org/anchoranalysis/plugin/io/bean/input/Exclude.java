/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input;

import java.util.List;
import java.util.ListIterator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Excludes all inputs that match a regular expression
 *
 * @author Owen Feehan
 * @param <T>
 */
public class Exclude<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private InputManager<T> input;

    @BeanField private RegEx regEx;
    // END BEAN PROPERITES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        List<T> list = input.inputObjects(params);

        ListIterator<T> itr = list.listIterator();
        while (itr.hasNext()) {

            if (regEx.hasMatch(itr.next().descriptiveName())) {
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

    public RegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(RegEx regEx) {
        this.regEx = regEx;
    }
}
