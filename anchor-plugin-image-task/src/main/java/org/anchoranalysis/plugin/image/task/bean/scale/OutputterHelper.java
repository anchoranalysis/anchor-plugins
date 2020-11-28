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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.identifier.provider.NamedProvider;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.stack.output.NamedStacksOutputter;
import org.anchoranalysis.image.io.stack.output.generator.StackGenerator;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.io.output.writer.ElementSupplier;
import org.anchoranalysis.io.output.writer.ElementWriterSupplier;
import org.anchoranalysis.io.output.writer.Writer;

/**
 * Write stacks to the filesystem according to certain outputting rules.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class OutputterHelper {

    /**
     * Is a particular first level output enabled?
     *
     * @param outputName the first-level output name
     * @param context input-output context
     * @return true iff the particular output is enabled.
     */
    public static boolean isFirstLevelOutputEnabled(String outputName, InputOutputContext context) {
        return context.getOutputter().outputsEnabled().isOutputEnabled(outputName);
    }

    /**
     * Is a particular second level output enabled?
     *
     * @param outputNameFirstLevel the first-level output name
     * @param keySecondLevel the second level unique key
     * @param context input-output context
     * @return true iff the particular output is enabled.
     */
    public static boolean isSecondLevelOutputEnabled(
            String outputNameFirstLevel, String keySecondLevel, InputOutputContext context) {
        return context.getOutputter()
                .outputsEnabled()
                .second(outputNameFirstLevel)
                .isOutputEnabled(keySecondLevel);
    }

    /**
     * Write stacks (some or all of a {@link DualNamedStacks} to the filesystem.
     *
     * @param dualStacks the stacks, a subset of which, may be written.
     * @param dualEnabled which stacks of {@code dualStacks} will be written.
     * @param outputNameNonFlattened the output-name to use for the non-flattened stacks.
     * @param outputNamedFlattened the output-name to use for the flattened stacks.
     * @param outputter the outputter to use for writing the stacks
     * @throws JobExecutionException
     */
    public static void outputStacks(
            DualNamedStacks dualStacks,
            DualEnabled dualEnabled,
            String outputNameNonFlattened,
            String outputNamedFlattened,
            OutputterChecked outputter)
            throws JobExecutionException {
        outputStacks(
                dualStacks.nonFlattened(),
                outputNameNonFlattened,
                outputter,
                !dualEnabled.isFlattened(),
                false);
        outputStacks(
                dualStacks.flattened(),
                outputNamedFlattened,
                outputter,
                !dualEnabled.isNonFlattened(),
                true);
    }

    /**
     * Outputs as a directory if there are multiple stacks, or as a single-stack if there is just
     * one.
     */
    private static void outputStacks(
            NamedProvider<Stack> stacks,
            String outputName,
            OutputterChecked outputter,
            boolean suppressOutputNameIfPossible,
            boolean always2D)
            throws JobExecutionException {
        try {
            int numberStacks = stacks.keys().size();

            if (numberStacks > 1) {
                // Output multiple stacks in a subdirectory (in the working directory for the input)
                NamedStacksOutputter.output(stacks, outputName, false, outputter);
            } else if (numberStacks == 1) {
                // Output a single stack in the working directory (for the input)
                outputSingleStack(
                        stacks,
                        outputName,
                        outputter.getWriters().permissive(),
                        suppressOutputNameIfPossible,
                        always2D);
            }
        } catch (OutputWriteFailedException e) {
            throw new JobExecutionException(
                    "Failed to write a particular stack in: " + outputName, e);
        }
    }

    /** Writes a single-stack <i>only</i> to the filesystem. */
    private static void outputSingleStack(
            NamedProvider<Stack> stacks,
            String outputName,
            Writer writer,
            boolean suppressOutputNameIfPossible,
            boolean always2D)
            throws OutputWriteFailedException {
        ElementWriterSupplier<Stack> writerSupplier =
                () -> new StackGenerator("scaledImage", always2D);
        ElementSupplier<Stack> elementSupplier = () -> extractArbitraryStack(stacks);
        if (suppressOutputNameIfPossible) {
            writer.writeWithoutName(outputName, writerSupplier, elementSupplier);
        } else {
            writer.write(outputName, writerSupplier, elementSupplier);
        }
    }

    /** Extracts any arbtirary stack from a named-provider. */
    private static Stack extractArbitraryStack(NamedProvider<Stack> stacks) {
        try {
            return stacks.getArbitraryElement();
        } catch (OperationFailedException e) {
            throw new AnchorImpossibleSituationException();
        }
    }
}
