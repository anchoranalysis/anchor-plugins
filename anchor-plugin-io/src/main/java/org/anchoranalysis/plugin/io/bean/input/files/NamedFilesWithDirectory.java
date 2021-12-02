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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithDirectory;
import org.anchoranalysis.io.input.file.FileInput;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;

/**
 * Like {@link NamedFiles} but rather accepts a {@link FileWithDirectoryInput} rather than a {@link
 * FileInput}.
 *
 * @author Owen Feehan
 */
public class NamedFilesWithDirectory extends NamedFilesBase<FileWithDirectoryInput> {

    // START BEAN PROPERTIES
    /** The files to use as inputs. */
    @BeanField @Getter @Setter private FilesProviderWithDirectory files;
    // END BEAN PROPERTIES

    @Override
    public InputsWithDirectory<FileWithDirectoryInput> inputs(InputManagerParameters parameters)
            throws InputReadFailedException {
        return createInputsFromFiles(
                files,
                parameters,
                namedFile ->
                        new FileWithDirectoryInput(
                                namedFile, files.getDirectoryAsPath(parameters.getInputContext())));
    }
}
