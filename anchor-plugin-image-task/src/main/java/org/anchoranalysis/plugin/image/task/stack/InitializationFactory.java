/*-
 * #%L
 * anchor-image-io
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

package org.anchoranalysis.plugin.image.task.stack;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.io.InitializationContext;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitialization;
import org.anchoranalysis.image.io.ImageInitializationFactory;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;

/** Factory for creating {@link ImageInitialization} instances. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InitializationFactory {

    /**
     * Creates an {@link ImageInitialization} with stacks added from the input.
     *
     * @param input the input that provides stacks
     * @param context the initialization context
     * @return a new {@link ImageInitialization} instance with stacks added
     * @throws OperationFailedException if the operation fails
     */
    public static ImageInitialization createWithStacks(
            ProvidesStackInput input, InitializationContext context)
            throws OperationFailedException {
        ImageInitialization initialization = createWithoutStacks(context);
        input.addToStoreInferNames(initialization.stacks(), context.getLogger());
        return initialization;
    }

    /**
     * Creates an {@link ImageInitialization} without adding any stacks.
     *
     * @param context the initialization context
     * @return a new {@link ImageInitialization} instance without stacks
     */
    public static ImageInitialization createWithoutStacks(InitializationContext context) {
        return ImageInitializationFactory.create(
                context.getInputOutput(), context.getSuggestedSize());
    }
}
