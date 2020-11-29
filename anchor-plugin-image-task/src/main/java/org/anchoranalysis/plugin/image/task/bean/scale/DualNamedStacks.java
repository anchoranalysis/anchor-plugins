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
package org.anchoranalysis.plugin.image.task.bean.scale;

import lombok.Value;
import lombok.experimental.Accessors;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.core.stack.named.NamedStacks;

/**
 * Named-collection of stacks, remembering either or both of a stack and it's maximum-intensity
 * projection.
 *
 * @author Owen Feehan
 */
@Value
@Accessors(fluent = true)
class DualNamedStacks {
    /** Named-collection of non-flattened stacks. */
    private NamedStacks nonFlattened = new NamedStacks();

    /** Named-collection of flattened stacks. */
    private NamedStacks flattened = new NamedStacks();

    /**
     * Adds a stack and/or it's maximum-intensity projection to the collection based on {@code
     * dualEnabled}.
     *
     * @param name name of the stack
     * @param stack the stack to add
     * @param dualEnabled whether non-flattend and flattened outputs are enabled.
     */
    public void addStack(String name, Stack stack, DualEnabled dualEnabled) {
        if (dualEnabled.isNonFlattened()) {
            nonFlattened.add(name, stack);
        }

        if (dualEnabled.isFlattened()) {
            flattened.add(name, stack.projectMax());
        }
    }
}
