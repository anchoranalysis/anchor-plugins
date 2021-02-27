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
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.NamedProvider;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.bean.init.MarksInitialization;
import org.anchoranalysis.mpp.io.input.InputForMarksBean;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;

/**
 * Like a {@link Define} but outputs also MPP-related data objects and an energy-stack.
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
public class DefineOutputterMPPWithEnergy extends DefineOutputterWithEnergy {

    /**
     * @author Owen Feehan
     * @param <T> init-params-type
     * @param <S> return-type
     */
    @FunctionalInterface
    public interface OperationWithEnergyStack<T, S> {
        S process(T initialization, EnergyStack energyStack) throws OperationFailedException;
    }

    public <S> S processInput(
            InputForMarksBean input,
            InitializationContext context,
            OperationWithEnergyStack<ImageInitialization, S> operation)
            throws OperationFailedException {

        try {
            MarksInitialization initialization = super.createInitialization(input, context);
            return processWithEnergyStack(
                    initialization.getImage(),
                    initialization.getImage(),
                    initialization,
                    operation,
                    context.getInputOutput());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    public <S> S processInput(
            InitializationContext context,
            Optional<NamedProvider<Stack>> stacks,
            Optional<NamedProvider<ObjectCollection>> objects,
            Optional<Dictionary> dictionary,
            OperationWithEnergyStack<MarksInitialization, S> operation)
            throws OperationFailedException {
        try {
            MarksInitialization initialization =
                    super.createInitialization(context, stacks, objects, dictionary);
            return processWithEnergyStack(
                    initialization,
                    initialization.getImage(),
                    initialization,
                    operation,
                    context.getInputOutput());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private <T, S> S processWithEnergyStack(
            T initialization,
            ImageInitialization imageParams,
            MarksInitialization mppParams,
            OperationWithEnergyStack<T, S> operation,
            InputOutputContext context)
            throws OperationFailedException {
        try {
            EnergyStack energyStack = super.createEnergyStack(imageParams, context.getLogger());

            S result = operation.process(initialization, energyStack);

            outputSharedObjects(mppParams, energyStack, context.getOutputter());

            return result;

        } catch (InitException | CreateException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    // General objects can be outputted
    private void outputSharedObjects(
            MarksInitialization initialization, EnergyStack energyStack, Outputter outputter)
            throws OutputWriteFailedException {

        super.outputSharedObjects(initialization, outputter.getChecked());

        new EnergyStackWriter(energyStack, outputter).writeEnergyStack();
    }
}
