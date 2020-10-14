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
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.descriptivename.FileNamer;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.files.FileInput;
import org.anchoranalysis.io.input.files.FilesProviderException;
import org.anchoranalysis.plugin.io.bean.descriptivename.RemoveExtensions;
import org.anchoranalysis.plugin.io.bean.descriptivename.patternspan.PatternSpan;

/**
 * File-paths
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class NamedFiles extends InputManager<FileInput> {

    // START BEAN PROPERTIES
    /** The files to use as inputs. */
    @BeanField @Getter @Setter private FilesProvider filesProvider;

    @BeanField @Getter @Setter
    private FileNamer namer = new RemoveExtensions(new PatternSpan());
    // END BEAN PROPERTIES

    public NamedFiles(FilesProvider filesProvider) {
        this.filesProvider = filesProvider;
    }

    @Override
    public List<FileInput> inputs(InputManagerParams params) throws InputReadFailedException {
        try {
            Collection<File> files = filesProvider.create(params);

            return FunctionalList.mapToList(
                    namer.deriveNameUnique(
                            files, params.getLogger()),
                    FileInput::new);
        } catch (FilesProviderException e) {
            throw new InputReadFailedException("Cannot find files", e);
        }
    }
}
