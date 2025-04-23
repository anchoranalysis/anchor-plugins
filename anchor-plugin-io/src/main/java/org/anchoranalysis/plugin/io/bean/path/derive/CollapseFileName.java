/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.io.bean.path.derive;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.bean.path.Identity;
import org.anchoranalysis.io.input.path.DerivePathException;

/**
 * Removes the file-name from a path, but keeps the directories and preserves the file-extension.
 *
 * <p>Specifically, a file-path of form {@code somedirectory/somename.ext} and converts to {@code
 * somedirectory.ext}.
 *
 * @author Owen Feehan
 */
public class CollapseFileName extends DerivePath {

    // START BEAN FIELDS
    /** Called as a delegate to provide a {@code source} {@link Path}. */
    @BeanField @Getter @Setter private DerivePath derivePath = new Identity();

    /** When true, the extension is appended to the directory. */
    @BeanField @Getter @Setter private boolean keepExtension = false;

    // END BEAN FIELDS

    @Override
    public Path deriveFrom(Path source, boolean debugMode) throws DerivePathException {
        Path path = derivePath.deriveFrom(source, debugMode);
        try {
            return createDerivedPath(path);
        } catch (CreateException e) {
            throw new DerivePathException(e);
        }
    }

    /** Creates the derived-path. */
    private Path createDerivedPath(Path source) throws CreateException {

        PathTwoParts pathParts = new PathTwoParts(source);

        if (keepExtension) {
            Optional<String> extension = ExtensionUtilities.extractExtension(pathParts.getSecond());
            return ExtensionUtilities.appendExtension(pathParts.getFirst(), extension);
        } else {
            return pathParts.getFirst();
        }
    }
}
