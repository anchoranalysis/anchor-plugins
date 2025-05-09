/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.file;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.AnnotationWithStrategy;
import org.anchoranalysis.annotation.io.bean.AnnotationInputManager;
import org.anchoranalysis.annotation.io.bean.AnnotatorStrategy;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInputPart;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithoutDirectory;
import org.anchoranalysis.io.input.file.FilesProviderException;

/**
 * Provides files associated with annotations.
 *
 * @param <T> the type of {@link AnnotatorStrategy} used.
 */
public class FromAnnotations<T extends AnnotatorStrategy> extends FilesProviderWithoutDirectory {

    // START BEAN PROPERTIES
    /** The annotation input manager. */
    @BeanField @Getter @Setter
    private AnnotationInputManager<NamedChannelsInputPart, T> annotations;

    // END BEAN PROPERTIES

    @Override
    public List<File> create(InputManagerParameters parameters) throws FilesProviderException {
        try {
            List<AnnotationWithStrategy<T>> inputs = annotations.inputs(parameters).inputs();
            return FunctionalList.flatMapToList(
                    inputs, input -> input.allAssociatedPaths().stream().map(Path::toFile));
        } catch (InputReadFailedException e) {
            throw new FilesProviderException(e);
        }
    }
}
