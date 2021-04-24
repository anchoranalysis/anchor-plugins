/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.bean.input.files;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.input.InputContextParams;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.FilesProviderException;
import org.anchoranalysis.io.input.file.NamedFile;

/**
 * Base class for an input-manager that produces inputs that are created from a {@link NamedFile}.
 *
 * @author Owen Feehan
 * @param <T> input-type
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class NamedFilesBase<T extends InputFromManager> extends InputManager<T> {

    // START BEAN PROPERTIES
    @BeanField @DefaultInstance @Getter @Setter private FileNamer namer;
    // END BEAN PROPERTIES

    /**
     * Creates a list of inputs from a {@link FilesProvider} which together with the {@code namer}
     * can create the inputs.
     *
     * @param files the files-provider
     * @param params parameters for the input-manager
     * @param mapToInput a function that maps a created {@link NamedFile} to the eventual
     *     input-type.
     * @return a newly created list of inputs
     * @throws InputReadFailedException
     */
    protected InputsWithDirectory<T> createInputsFromFiles(
            FilesProvider files, InputManagerParams params, Function<NamedFile, T> mapToInput)
            throws InputReadFailedException {
        try {
            Collection<File> filesCreated = files.create(params);

            Optional<Path> inputDirectory = inputDirectory(files, params.getInputContext());
            
            FileNamerContext context =
                    new FileNamerContext(
                            inputDirectory,
                            params.getInputContext().isRelativeForIdentifier(),
                            params.getLogger());
            
            return new InputsWithDirectory<>( createInputs(filesCreated, mapToInput, context), inputDirectory);
        } catch (FilesProviderException e) {
            throw new InputReadFailedException("Cannot find files", e);
        }
    }
    
    private List<T> createInputs(Collection<File> filesCreated, Function<NamedFile, T> mapToInput, FileNamerContext context) throws InputReadFailedException {
        return FunctionalList.mapToList(
                namer.deriveNameUnique(filesCreated, context), mapToInput);
    }

    private static Optional<Path> inputDirectory(FilesProvider files, InputContextParams context)
            throws FilesProviderException {
        return OptionalUtilities.orElseGetFlat(
                context.getInputDirectory(), () -> files.rootDirectory(context));
    }
}
