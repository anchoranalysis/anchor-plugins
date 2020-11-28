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
package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedRaster;

/**
 * If the extension(s) of a path matches a particular value, then use a particular reader, otherwise
 * a fallback.
 *
 * @author Owen Feehan
 */
public class BranchExtension extends StackReader {

    // START BEAN PROPERTIES
    /** Any extensions to match (case insensitive) not including any leading period. */
    @BeanField @Getter @Setter private StringSet extensions;

    /** The reader to use if the extension matches. */
    @BeanField @Getter @Setter private StackReader readerMatching;

    /** The reader to use if the extension does not match. */
    @BeanField @Getter @Setter private StackReader readerNotMatching;
    // END BEAN PROPERTIES

    /** Lazily create a lowercase version of extensions, as first needed. */
    private Set<String> extensionsLowercase;

    @Override
    public OpenedRaster openFile(Path path) throws ImageIOException {
        if (doesPathHaveExtension(path)) {
            return readerMatching.openFile(path);
        } else {
            return readerNotMatching.openFile(path);
        }
    }

    private void createLowercaseExtensionsIfNecessary() {
        if (extensionsLowercase == null) {
            extensionsLowercase =
                    extensions.stream().map(String::toLowerCase).collect(Collectors.toSet());
        }
    }

    private boolean doesPathHaveExtension(Path path) {
        createLowercaseExtensionsIfNecessary();
        String pathLowercase = path.toString().toLowerCase();
        return extensionsLowercase.stream().anyMatch(pathLowercase::endsWith);
    }
}
