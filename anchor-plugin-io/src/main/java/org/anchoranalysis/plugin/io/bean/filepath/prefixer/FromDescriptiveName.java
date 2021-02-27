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

package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.output.bean.path.prefixer.PathPrefixerAvoidResolve;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.io.output.path.prefixer.NamedPath;
import org.anchoranalysis.io.output.path.prefixer.PathPrefixerContext;

/**
 * The prefixer uses a combination of a out-path-prefix and the descriptive-name of inputs to create
 * an output prefix.
 *
 * @author Owen Feehan
 */
public class FromDescriptiveName extends PathPrefixerAvoidResolve {

    /** The Unix separator character. */
    private static final char UNIX_SEPARATOR = '/';

    /** The Windows separator character. */
    private static final char WINDOWS_SEPARATOR = '\\';

    /** The character to replace directory separators with when suppressed. */
    private static final char REPLACEMENT_CHARACTER = '_';

    // START BEAN PROPERTIES
    /**
     * If true, any directory separators in the identifier in a descriptive-name are replaced by
     * underscores.
     *
     * <p>If false, this may still occur if it is requested in the {@link PathPrefixerContext}.
     */
    @BeanField @Getter @Setter private boolean suppressDirectories = false;
    // END BEAN PROPERTIES

    @Override
    public DirectoryWithPrefix outFilePrefixFromPath(
            NamedPath path, Path root, PathPrefixerContext context) {
        Path combined = root.resolve(Paths.get(calculateIdentifierForPrefix(path, context)));
        return new DirectoryWithPrefix(combined);
    }

    private String calculateIdentifierForPrefix(NamedPath path, PathPrefixerContext context) {
        String identifier = path.getName();

        if (suppressDirectories || context.getPrefixer().isOutputSuppressDirectories()) {
            return replaceSeperatorsWithUnderscore(identifier);
        } else {
            return identifier;
        }
    }

    private static String replaceSeperatorsWithUnderscore(String string) {
        return string.replace(WINDOWS_SEPARATOR, REPLACEMENT_CHARACTER)
                .replace(UNIX_SEPARATOR, REPLACEMENT_CHARACTER);
    }
}
