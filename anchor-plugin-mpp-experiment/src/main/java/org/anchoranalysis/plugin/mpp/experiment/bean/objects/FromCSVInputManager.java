/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVInputObject;

// An input stack
public class FromCSVInputManager extends InputManager<FromCSVInputObject> {

    // START BEAN PROPERTIES
    @BeanField private MultiInputManager input;

    @BeanField private FilePathGenerator appendCSV;
    // END BEAN PROPERTIES

    @Override
    public List<FromCSVInputObject> inputObjects(InputManagerParams params)
            throws AnchorIOException {

        Iterator<MultiInput> itr = input.inputObjects(params).iterator();

        List<FromCSVInputObject> out = new ArrayList<>();

        while (itr.hasNext()) {
            MultiInput inputObj = itr.next();

            Path csvFilePathOut =
                    appendCSV.outFilePath(
                            inputObj.pathForBindingRequired(), params.isDebugModeActivated());
            out.add(new FromCSVInputObject(inputObj, csvFilePathOut));
        }

        return out;
    }

    public FilePathGenerator getAppendCSV() {
        return appendCSV;
    }

    public void setAppendCSV(FilePathGenerator appendCSV) {
        this.appendCSV = appendCSV;
    }

    public MultiInputManager getInput() {
        return input;
    }

    public void setInput(MultiInputManager input) {
        this.input = input;
    }
}
