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
/* (C)2020 */
package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStack;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPPWithNrg;

class DefineFixture {

    private DefineFixture() {}

    /**
     * Creates a DefineOutputter
     *
     * @param nrgStack the nrg-stack associated with the define
     * @param sharedFeatures any shared to be added to the define
     * @return
     * @throws CreateException
     */
    public static DefineOutputterMPPWithNrg create(
            NRGStack nrgStack,
            Optional<List<NamedBean<FeatureListProvider<FeatureInput>>>> sharedFeatures)
            throws CreateException {
        DefineOutputterMPPWithNrg out = new DefineOutputterMPPWithNrg();
        out.setNrgStackProvider(nrgStackProvider(nrgStack));
        out.setDefine(createDefine(sharedFeatures));
        return out;
    }

    private static Define createDefine(
            Optional<List<NamedBean<FeatureListProvider<FeatureInput>>>> sharedFeatures)
            throws CreateException {
        Define define = new Define();

        if (sharedFeatures.isPresent()) {
            for (NamedBean<FeatureListProvider<FeatureInput>> nb : sharedFeatures.get()) {
                try {
                    define.add(nb);
                } catch (OperationFailedException e) {
                    throw new CreateException(e);
                }
            }
        }

        return define;
    }

    private static StackProvider nrgStackProvider(NRGStack nrgStack) throws CreateException {

        // Create NRG stack
        Stack stack = nrgStack.asStack();

        // Encapsulate in a mock
        StackProvider stackProvider = mock(StackProvider.class);
        when(stackProvider.create()).thenReturn(stack);
        when(stackProvider.duplicateBean()).thenReturn(stackProvider);
        return stackProvider;
    }
}
