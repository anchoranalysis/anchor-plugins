package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.provider.store.NamedProviderStore;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.io.stack.input.TimeSequenceSupplier;
import org.anchoranalysis.image.io.stack.time.TimeSequence;

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

    /** The image read from the file-system. */
    private final Stack stack;

    /**
     * Create for a particular directory and filename.
     *
     * @param directory the directory of the image to be read.
     * @param filename the filename of the image to be read.
     * @param reader how to read the image.
     * @param context context for reading the image.
     * @throws ImageIOException if the image cannot be successfully read.
     */
    public StackSequenceInputFixture(
            Path directory, String filename, StackReader reader, OperationContext context)
            throws ImageIOException {
        this.filename = filename;
        this.path = directory.resolve(filename);
        this.stack = reader.readStack(path, context);
    }

    @Override
    public void addToStoreInferNames(
            NamedProviderStore<TimeSequence> stacks,
            int seriesIndex,
            Progress progress,
            Logger logger)
            throws OperationFailedException {
        addToStoreWithName(DEFAULT_STACK_IDENTIFIER, stacks, seriesIndex, progress, logger);
    }

    @Override
    public void addToStoreWithName(
            String name,
            NamedProviderStore<TimeSequence> stacks,
            int seriesIndex,
            Progress progress,
            Logger logger)
            throws OperationFailedException {
        stacks.add(name, () -> createTimeSequence());
    }

    @Override
    public int numberFrames() throws OperationFailedException {
        return 1;
    }

    @Override
    public String identifier() {
        return filename;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(path);
    }

    @Override
    public TimeSequenceSupplier createStackSequenceForSeries(int seriesIndex, Logger logger)
            throws ImageIOException {
        return progress -> createTimeSequence();
    }

    private TimeSequence createTimeSequence() {
        return new TimeSequence(stack);
    }
}
