/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.mpp.experiment.objects.FromCSVInput;

// An input stack
public class FromCSVInputManager extends InputManager<FromCSVInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MultiInputManager input;

    @BeanField @Getter @Setter private DerivePath appendCSV;
    // END BEAN PROPERTIES

    @Override
    public InputsWithDirectory<FromCSVInput> inputs(InputManagerParameters parameters)
            throws InputReadFailedException {

        InputsWithDirectory<MultiInput> inputs = input.inputs(parameters);

        Iterator<MultiInput> itr = inputs.iterator();

        List<FromCSVInput> out = new ArrayList<>();

        while (itr.hasNext()) {
            MultiInput inputObj = itr.next();

            try {
                Path csvFilePathOut =
                        appendCSV.deriveFrom(
                                inputObj.pathForBindingRequired(),
                                parameters.isDebugModeActivated());
                out.add(new FromCSVInput(inputObj, csvFilePathOut));
            } catch (DerivePathException e) {
                throw new InputReadFailedException(e);
            }
        }

        return inputs.withInputs(out);
    }
}
