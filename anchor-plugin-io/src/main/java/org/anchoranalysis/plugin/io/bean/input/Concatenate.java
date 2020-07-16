/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;

/**
 * Concatenates several input-managers
 *
 * @author Owen Feehan
 * @param <T> input-object type
 */
public class Concatenate<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField private List<InputManager<T>> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public List<T> inputObjects(InputManagerParams params) throws AnchorIOException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), list.size())) {

            ArrayList<T> listOut = new ArrayList<>();

            for (InputManager<T> inputManager : list) {
                listOut.addAll(inputManager.inputObjects(params));

                prm.incrWorker();
            }
            return listOut;
        }
    }

    public List<InputManager<T>> getList() {
        return list;
    }

    public void setList(List<InputManager<T>> list) {
        this.list = list;
    }
}
