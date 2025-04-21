/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Set;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/**
 * Associates a label with an image.
 *
 * <p>This can be used to associate labels with images for training or evaluation in a
 * machine-learning problem.
 *
 * @param <T> the type of shared-state used by the labeller
 */
public abstract class ImageLabeller<T> extends AnchorBean<ImageLabeller<T>> {

    /**
     * Initializes the labeller. Should be called once before calling any other methods.
     *
     * @param pathForBinding a {@link Path} that can be used by the labeller to make file path
     *     decisions
     * @return the initialized shared-state of type {@code T}
     * @throws InitializeException if initialization fails
     */
    public abstract T initialize(Path pathForBinding) throws InitializeException;

    /**
     * Returns a set of identifiers for all groups that can be outputted by the labeller.
     *
     * <p>This method should be callable at any time.
     *
     * @param initialization the initialized shared-state returned by {@link #initialize(Path)}
     * @return a {@link Set} of {@link String} labels
     */
    public abstract Set<String> allLabels(T initialization);

    /**
     * Determines a particular group-identifier (label) for an input.
     *
     * @param sharedState the shared-state returned by {@link #initialize(Path)}
     * @param input the {@link ProvidesStackInput} to be labelled
     * @param context the {@link InputOutputContext} for the operation
     * @return the label as a {@link String}
     * @throws OperationFailedException if the labelling operation fails
     */
    public abstract String labelFor(
            T sharedState, ProvidesStackInput input, InputOutputContext context)
            throws OperationFailedException;
}
