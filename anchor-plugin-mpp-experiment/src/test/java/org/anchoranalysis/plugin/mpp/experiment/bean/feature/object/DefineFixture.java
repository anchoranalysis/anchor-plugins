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

package org.anchoranalysis.plugin.mpp.experiment.bean.feature.object;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.define.DefineAddException;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParameters;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.plugin.mpp.bean.define.DefineOutputterWithEnergy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DefineFixture {

    /**
     * Creates a {@link DefineOutputterWithEnergy}.
     *
     * @param energyStack the energy-stack associated with the define
     * @param sharedFeatures any shared to be added to the define
     * @return
     * @throws ProvisionFailedException
     */
    public static DefineOutputterWithEnergy create(
            EnergyStackWithoutParameters energyStack,
            Optional<List<NamedBean<FeatureListProvider<FeatureInput>>>> sharedFeatures)
            throws ProvisionFailedException {
        DefineOutputterWithEnergy out = new DefineOutputterWithEnergy();
        out.setStackEnergy(stackEnergy(energyStack));
        out.setDefine(createDefine(sharedFeatures));
        return out;
    }

    private static Define createDefine(
            Optional<List<NamedBean<FeatureListProvider<FeatureInput>>>> sharedFeatures)
            throws ProvisionFailedException {
        Define define = new Define();

        if (sharedFeatures.isPresent()) {
            for (NamedBean<FeatureListProvider<FeatureInput>> namedBean : sharedFeatures.get()) {
                try {
                    define.add(namedBean);
                } catch (DefineAddException e) {
                    throw new ProvisionFailedException(e);
                }
            }
        }

        return define;
    }

    private static StackProvider stackEnergy(EnergyStackWithoutParameters energyStack)
            throws ProvisionFailedException {

        // Create energy stack
        Stack stack = energyStack.asStack();

        // Encapsulate in a mock
        StackProvider provider = mock(StackProvider.class);
        when(provider.get()).thenReturn(stack);
        when(provider.duplicateBean()).thenReturn(provider);
        return provider;
    }
}