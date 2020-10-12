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

package org.anchoranalysis.plugin.quick.bean.file.path.derive;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import lombok.Getter;

/** Separates a path into two parts */
class PathTwoParts {
    @Getter private Path first;
    @Getter private Path second;

    public PathTwoParts(Path path) throws CreateException {
        checkPathHasParent(path);

        if (path.endsWith("/")) {
            first = path;
            second = null;
        } else {
            first = path.getParent();
            second = path.getFileName();
        }
    }

    /** Removes the last directory from the dir */
    public void removeLastDirectory() {
        first = first.getParent();
    }

    /** Removes the last directory in dir to rest */
    public void moveLastDirectoryToRest() {
        Path name = first.getFileName();
        second = name.resolve(second);
        first = first.getParent();
    }

    public Path combine() {
        return first.resolve(second);
    }

    private static void checkPathHasParent(Path path) throws CreateException {

        if (path.getParent() == null) {
            throw new CreateException(String.format("No parent exists for %s", path));
        }
    }
}
