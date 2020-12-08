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
import org.anchoranalysis.core.value.KeyValueParams;
import org.anchoranalysis.experiment.io.InitParamsContext;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.mpp.io.input.InputForMPPBean;
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
        S process(T initParams, EnergyStack energyStack) throws OperationFailedException;
    }

    public <S> S processInput(
            InputForMPPBean input,
            InitParamsContext context,
            OperationWithEnergyStack<ImageInitParams, S> operation)
            throws OperationFailedException {

        try {
            MPPInitParams initParams = super.createInitParams(input, context);
            return processWithEnergyStack(
                    initParams.getImage(), initParams.getImage(), initParams, operation, context.getInputOutput());
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    public <S> S processInput(
            InitParamsContext context,
            Optional<NamedProvider<Stack>> stacks,
            Optional<NamedProvider<ObjectCollection>> objects,
            Optional<KeyValueParams> keyValueParams,
            OperationWithEnergyStack<MPPInitParams, S> operation)
            throws OperationFailedException {
        try {
            MPPInitParams initParams =
                    super.createInitParams(context, stacks, objects, keyValueParams);
            return processWithEnergyStack(
                    initParams, initParams.getImage(), initParams, operation, context.getInputOutput());

        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private <T, S> S processWithEnergyStack(
            T initParams,
            ImageInitParams imageParams,
            MPPInitParams mppParams,
            OperationWithEnergyStack<T, S> operation,
            InputOutputContext context)
            throws OperationFailedException {
        try {
            EnergyStack energyStack = super.createEnergyStack(imageParams, context.getLogger());

            S result = operation.process(initParams, energyStack);

            outputSharedObjects(mppParams, energyStack, context.getOutputter());

            return result;

        } catch (InitException | CreateException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    // General objects can be outputted
    private void outputSharedObjects(
            MPPInitParams initParams, EnergyStack energyStack, Outputter outputter)
            throws OutputWriteFailedException {

        super.outputSharedObjects(initParams, outputter.getChecked());

        new EnergyStackWriter(energyStack, outputter).writeEnergyStack();
    }
}
