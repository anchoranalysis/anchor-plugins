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

package org.anchoranalysis.plugin.image.task.feature.fixture;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.image.io.stack.time.TimeSeries;

/**
 * A fixture that implements {@link ProvidesStackInput} for testing purposes. It provides a single
 * {@link Stack} as a {@link TimeSeries}.
 */
@AllArgsConstructor
public class StackAsProviderFixture implements ProvidesStackInput {

    /** The constant string used as an identifier for the stack. */
    public static final String IDENTIFIER = "arbitraryIdentifier";

    /** The stack to be provided. */
    private Stack stack;

    /** The name to be used when adding the stack to a store. */
    private String name;

    @Override
    public String identifier() {
        return IDENTIFIER;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(Paths.get("arbitraryPath/"));
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        addToStoreWithName(name, stacks, 0, logger);
    }

    @Override
    public void addToStoreWithName(
            String name, NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        stacks.add(name, () -> new TimeSeries(stack));
    }

    @Override
    public int numberFrames() throws OperationFailedException {
        return 1;
    }
}
