/*-
 * #%L
 * anchor-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.bean.define;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.core.stack.StackIdentifiers;
import org.anchoranalysis.mpp.segment.bean.define.DefineOutputter;
import org.anchoranalysis.plugin.image.provider.ReferenceFactory;

public abstract class DefineOutputterWithEnergy extends DefineOutputter {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private StackProvider stackEnergy = ReferenceFactory.stack(StackIdentifiers.ENERGY_STACK);

    @BeanField @OptionalBean @Getter @Setter private DictionaryProvider dictionary;
    // END BEAN PROPERTIES

    protected EnergyStack createEnergyStack(ImageInitialization initialization, Logger logger)
            throws InitException, CreateException {

        // Extract the energy stack
        StackProvider stackDuplicated = stackEnergy.duplicateBean();
        stackDuplicated.initRecursive(initialization, logger);
        EnergyStack stack = new EnergyStack(stackDuplicated.create());

        if (dictionary != null) {
            dictionary.initRecursive(initialization.dictionaryInitialization(), logger);
            stack.setDictionary(dictionary.create());
        }
        return stack;
    }
}
