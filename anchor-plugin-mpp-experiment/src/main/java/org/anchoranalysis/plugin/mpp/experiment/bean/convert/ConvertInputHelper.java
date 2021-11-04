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
package org.anchoranalysis.plugin.mpp.experiment.bean.convert;

import java.nio.file.Path;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.image.io.stack.input.ImageMetadataInput;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.input.file.NamedFile;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.plugin.io.bean.input.stack.ConvertNamedChannelsInputToStack;

/**
 * Converts an input to the expected input-type if necessary and if possible.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ConvertInputHelper {

    public static <T extends NamedChannelsInput> InputFromManager convert(
            T input, InputTypesExpected inputTypesExpected, Optional<Path> directory)
            throws ExperimentExecutionException {
        Class<? extends InputFromManager> inputClass = input.getClass();

        if (inputTypesExpected.doesClassInheritFromAny(inputClass)) {
            // All good, the delegate happily accepts our type without change
            return input;
        } else if (inputTypesExpected.doesClassInheritFromAny(MultiInput.class)) {
            return new MultiInput(input);
        } else if (inputTypesExpected.doesClassInheritFromAny(StackSequenceInput.class)) {
            return new ConvertNamedChannelsInputToStack(input);
        } else if (inputTypesExpected.doesClassInheritFromAny(FileWithDirectoryInput.class)) {
            return convertToFileWithDirectory(input, directory);
        } else if (inputTypesExpected.doesClassInheritFromAny(ImageMetadataInput.class)) {
            return convertToImageMetadata(input);
        } else {
            throw new ExperimentExecutionException(
                    String.format(
                            "Cannot pass or convert the input-type (%s) to match the delegate's expected input-type:%n%s",
                            inputClass, inputTypesExpected));
        }
    }

    /**
     * Adds all the input-types that can exist after conversion.
     *
     * @param expected where the input-types are added to.
     */
    public static void addSupportedConversionInputTypes(InputTypesExpected expected) {
        // Add the other types we'll consider converting
        expected.add(MultiInput.class);
        expected.add(StackSequenceInput.class);
        expected.add(FileWithDirectoryInput.class);
        expected.add(ImageMetadataInput.class);
    }

    /** Converts to a {@link FileWithDirectoryInput}. */
    private static <T extends NamedChannelsInput> FileWithDirectoryInput convertToFileWithDirectory(
            T input, Optional<Path> directory) throws ExperimentExecutionException {
        try {
            NamedFile namedFile =
                    new NamedFile(input.identifier(), input.pathForBindingRequired().toFile());
            return new FileWithDirectoryInput(namedFile, directory.get()); // NOSONAR
        } catch (InputReadFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }

    /** Converts to a {@link ImageMetadataInput}. */
    private static <T extends NamedChannelsInput> ImageMetadataInput convertToImageMetadata(T input)
            throws ExperimentExecutionException {
        try {
            return new ImageMetadataInput(input.asFile(), input.metadata(0));
        } catch (ImageIOException | InputReadFailedException e) {
            throw new ExperimentExecutionException(e);
        }
    }
}
