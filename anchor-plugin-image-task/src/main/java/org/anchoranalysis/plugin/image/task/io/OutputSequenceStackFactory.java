package org.anchoranalysis.plugin.image.task.io;

import java.util.Optional;
import org.anchoranalysis.image.io.generator.raster.StackGenerator;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.generator.sequence.OutputSequence;
import org.anchoranalysis.io.generator.sequence.OutputSequenceDirectory;
import org.anchoranalysis.io.generator.sequence.OutputSequenceFactory;
import org.anchoranalysis.io.generator.sequence.OutputSequenceIncremental;
import org.anchoranalysis.io.generator.sequence.OutputSequenceNonIncremental;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Creates {@link OutputSequence} of different kinds for writing stacks to a directory.
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class OutputSequenceStackFactory {

    /** A factory with no restictions on what kind of stacks can be outputted. */
    public static final OutputSequenceStackFactory NO_RESTRICTIONS = new OutputSequenceStackFactory(
       new StackGenerator(false, Optional.empty(), false)
    );
    
    /**
     * The stacks that are outputted are guaranteed to be two-dimensional.
     * 
     * @param manifestFunction the manifest-function to use when outputting.
     * @return a newly created factory
     */
    public static OutputSequenceStackFactory always2D(String manifestFunction) {
        return new OutputSequenceStackFactory(
           new StackGenerator(manifestFunction, true)
        );
    }
    
    private final StackGenerator generator;
    
    /**
     * Creates an sequence of stacks in a subdirectory that has an incremental integer pattern in filename.
     * 
     * @param subdirectoryName the name of the sub-directory in which stacks will be written (relative to {@code context}.
     * @param context the current directory in which subdirectory will be created.
     * @return the created output-sequence (and started)
     * @throws OutputWriteFailedException
     */
    public OutputSequenceIncremental<Stack> incrementalSubdirectory(String subdirectoryName, InputOutputContext context) throws OutputWriteFailedException {
        return new OutputSequenceFactory<>(generator, context).incremental(new OutputSequenceDirectory(subdirectoryName));
    }

    /**
     * Creates a sequence of stacks in the current context's directory that has no incremental pattern in filenames.
     * 
     * @param context the current directory in which output is occurring.
     * @return the created output-sequence (and started)
     * @throws OutputWriteFailedException
     */
    public OutputSequenceNonIncremental<Stack> nonIncrementalCurrentDirectory(String prefix, InputOutputContext context) throws OutputWriteFailedException {
        return new OutputSequenceFactory<>(generator, context).nonIncrementalCurrentDirectory(prefix);
    }
}
