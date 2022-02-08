/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.time.TimeSeries;

/**
 * Implementation of {@link StackSequenceInput} that reads a {@link Stack} from a particular {@code
 * filename} in a particular {@code directory}.
 *
 * @author Owen Feehan
 */
public class StackSequenceInputFixture implements StackSequenceInput {

    /** Default stack identifier, if no other is provided. */
    private static final String DEFAULT_STACK_IDENTIFIER = "stack";

    /** The filename of the image that was read. */
    private final String filename;

    /** The path to the image that was read, combining directory and {@code filename}. */
    private final Path path;

    /** The identifier. */
    private final Optional<String> identifier;

    /** The image read from the file-system. */
    private final Stack stack;

    /**
     * Create by reading from the file-system.
     *
     * @param directory the directory of the image to be read.
     * @param filename the filename of the image to be read.
     * @param reader how to read the image.
     * @param identifier when defined, this is used as the identifier, otherwise the filename.
     * @param context context for reading the image.
     * @throws ImageIOException if the image cannot be successfully read.
     */
    public StackSequenceInputFixture(
            Path directory,
            String filename,
            StackReader reader,
            Optional<String> identifier,
            OperationContext context)
            throws ImageIOException {
        this.filename = filename;
        this.identifier = identifier;
        this.path = directory.resolve(filename);
        this.stack = reader.readStack(path, context);
    }

    /**
     * Create from an existing {@link Stack}.
     *
     * @param stack the stack.
     * @param directory the associated directory (which is not actually read, but helps for the
     *     associated input path).
     * @param filename the associated filename (which is not actually read, but helps for the
     *     associated input path).
     * @param identifier when defined, this is used as the identifier, otherwise the filename.
     */
    public StackSequenceInputFixture(
            Stack stack, String directory, String filename, Optional<String> identifier) {
        this.stack = stack;
        this.path = Paths.get(directory).resolve(filename);
        this.filename = filename;
        this.identifier = identifier;
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        addToStoreWithName(DEFAULT_STACK_IDENTIFIER, stacks, seriesIndex, logger);
    }

    @Override
    public void addToStoreWithName(
            String name, NamedProviderStore<TimeSeries> stacks, int seriesIndex, Logger logger)
            throws OperationFailedException {
        stacks.add(name, () -> createTimeSequence());
    }

    @Override
    public int numberFrames() throws OperationFailedException {
        return 1;
    }

    @Override
    public String identifier() {
        return identifier.orElse(filename);
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(path);
    }

    @Override
    public TimeSeries createStackSequenceForSeries(int seriesIndex, Logger logger)
            throws ImageIOException {
        return createTimeSequence();
    }

    private TimeSeries createTimeSequence() {
        return new TimeSeries(stack);
    }
}
