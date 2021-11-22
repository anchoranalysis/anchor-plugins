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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.bean.shared.dictionary.DictionaryProvider;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.SharedObjects;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.io.stack.StackIdentifiers;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.mpp.io.input.ExportSharedObjects;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;
import org.anchoranalysis.mpp.segment.bean.define.DefineOutputter;
import org.anchoranalysis.plugin.image.provider.ReferenceFactory;

/**
 * Like a {@link Define} but outputs also an energy-stack.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td rowspan="3"><i>outputs from {@link EnergyStackWriter}</i></td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class DefineOutputterWithEnergy extends DefineOutputter {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private StackProvider stackEnergy = ReferenceFactory.stack(StackIdentifiers.ENERGY_STACK);

    @BeanField @OptionalBean @Getter @Setter private DictionaryProvider dictionary;
    // END BEAN PROPERTIES

    /**
     * @author Owen Feehan
     * @param <T> init-params-type
     * @param <S> return-type
     */
    @FunctionalInterface
    public interface ProcessWithEnergyStack<T, S> {
        S process(T initialization, EnergyStack energyStack) throws OperationFailedException;
    }

    public <S> void processInput(
            ExportSharedObjects input,
            InitializationContext context,
            ProcessWithEnergyStack<ImageInitialization, S> operation)
            throws OperationFailedException {

        try {
            ImageInitialization initialization = super.createInitialization(context, input);
            processWithEnergyStack(initialization, operation, context.getInputOutput());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    public <S> S processInput(
            InitializationContext context,
            SharedObjects sharedObjects,
            Optional<Dictionary> dictionary,
            ProcessWithEnergyStack<ImageInitialization, S> operation)
            throws OperationFailedException {
        try {
            ImageInitialization initialization =
                    super.createInitialization(context, Optional.of(sharedObjects), dictionary);
            return processWithEnergyStack(initialization, operation, context.getInputOutput());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private <S> S processWithEnergyStack(
            ImageInitialization initialization,
            ProcessWithEnergyStack<ImageInitialization, S> operation,
            InputOutputContext context)
            throws OperationFailedException {
        try {
            EnergyStack energyStack = createEnergyStack(initialization, context.getLogger());

            S result = operation.process(initialization, energyStack);

            outputSharedObjects(
                    initialization.getSharedObjects(),
                    Optional.of(energyStack),
                    context.getOutputter());

            return result;

        } catch (InitializeException | ProvisionFailedException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    private EnergyStack createEnergyStack(ImageInitialization initialization, Logger logger)
            throws InitializeException, ProvisionFailedException {

        // Extract the energy stack
        StackProvider stackDuplicated = stackEnergy.duplicateBean();
        stackDuplicated.initializeRecursive(initialization, logger);
        EnergyStack stack = new EnergyStack(stackDuplicated.get());

        if (dictionary != null) {
            dictionary.initializeRecursive(initialization.dictionaryInitialization(), logger);
            stack.setDictionary(dictionary.get());
        }
        return stack;
    }
}
