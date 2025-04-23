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
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.define.Define;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.SharedObjects;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.mpp.io.input.ExportSharedObjects;
import org.anchoranalysis.mpp.io.input.MarksInitializationFactory;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.io.output.EnergyStackWriter;
import org.anchoranalysis.mpp.mark.Mark;

/**
 * Applies a {@link Define} on inputs and outputs produced entities (images, histograms, objects
 * etc.).
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>stacks</td><td>yes</td><td>Image-stacks that are produced.</td></tr>
 * <tr><td>objects</td><td>yes</td><td>Collections of {@link ObjectMask}s that are produced as HDF5</td></tr>
 * <tr><td>histograms</td><td>yes</td><td>Histograms that are produced as CSV.</td></tr>
 * <tr><td>marks</td><td>yes</td><td>Collections of {@link Mark}s that are produced as serialized XML.</td></tr>
 * </tbody>
 * </table>
 */
public class DefineOutputter extends AnchorBean<DefineOutputter> {

    /** The {@link Define} to be applied on inputs. */
    @BeanField @OptionalBean @Getter @Setter private Define define = new Define();

    /** If true, suppresses the creation of subfolders for outputs. */
    @BeanField @Getter @Setter private boolean suppressSubfolders = false;

    /**
     * Functional interface for processing an {@link ImageInitialization}.
     *
     * @param <T> the type of initialization to process
     */
    @FunctionalInterface
    public interface Processor<T> {
        /**
         * Processes the initialization.
         *
         * @param initialization the initialization to process
         * @throws OperationFailedException if the processing operation fails
         */
        void process(T initialization) throws OperationFailedException;
    }

    /**
     * Adds all possible output-names to a {@link OutputEnabledMutable}.
     *
     * @param outputEnabled where to add all possible output-names
     */
    public void addAllOutputNamesTo(OutputEnabledMutable outputEnabled) {
        SharedObjectsOutputter.addAllOutputNamesTo(outputEnabled);
    }

    /**
     * Processes the input using the provided operation.
     *
     * @param <S> the type of the input
     * @param input the input to process
     * @param operation the operation to apply on the initialization
     * @throws OperationFailedException if the operation fails
     */
    public <S> void process(
            InputBound<MultiInput, S> input, Processor<ImageInitialization> operation)
            throws OperationFailedException {
        InitializationContext context = input.createInitializationContext();
        try {
            ImageInitialization initialization = createInitialization(context, input.getInput());

            operation.process(initialization);

            outputSharedObjects(
                    initialization.sharedObjects(), Optional.empty(), context.getOutputter());

        } catch (CreateException | OutputWriteFailedException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Creates an {@link ImageInitialization} from the given context and input.
     *
     * @param context the initialization context
     * @param input the input to create the initialization from
     * @return the created {@link ImageInitialization}
     * @throws CreateException if the initialization creation fails
     */
    protected ImageInitialization createInitialization(
            InitializationContext context, ExportSharedObjects input) throws CreateException {
        return MarksInitializationFactory.create(
                        Optional.of(input), context, Optional.ofNullable(define))
                .image();
    }

    /**
     * Creates an {@link ImageInitialization} from the given context, shared objects, and
     * dictionary.
     *
     * @param context the initialization context
     * @param sharedObjects optional shared objects
     * @param dictionary optional dictionary
     * @return the created {@link ImageInitialization}
     * @throws CreateException if the initialization creation fails
     */
    protected ImageInitialization createInitialization(
            InitializationContext context,
            Optional<SharedObjects> sharedObjects,
            Optional<Dictionary> dictionary)
            throws CreateException {
        ImageInitialization initialization =
                MarksInitializationFactory.create(
                                Optional.empty(), context, Optional.ofNullable(define))
                        .image();
        try {
            initialization.addSharedObjectsDictionary(sharedObjects, dictionary);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
        return initialization;
    }

    /**
     * Outputs shared objects and optionally an energy stack.
     *
     * @param sharedObjects the shared objects to output
     * @param energyStack optional energy stack to output
     * @param outputter the outputter to use
     * @throws OutputWriteFailedException if the output operation fails
     */
    protected void outputSharedObjects(
            SharedObjects sharedObjects, Optional<EnergyStack> energyStack, Outputter outputter)
            throws OutputWriteFailedException {

        new SharedObjectsOutputter(sharedObjects, suppressSubfolders, outputter.getChecked())
                .output();

        if (energyStack.isPresent()) {
            new EnergyStackWriter(energyStack.get(), outputter).writeEnergyStack();
        }
    }
}
