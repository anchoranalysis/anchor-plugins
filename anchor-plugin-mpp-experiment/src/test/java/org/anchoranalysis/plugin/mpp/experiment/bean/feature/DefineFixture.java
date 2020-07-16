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
