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

package org.anchoranalysis.plugin.annotation.bean.strategy;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;

/** Utility class for deriving paths from a generator. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PathFromGenerator {

    /** The debug-mode for everything that isn't the main input */
    private static final boolean DEBUG_MODE_NON_INPUT = false;

    /**
     * Derives a path using a {@link DerivePath} generator and a binding path.
     *
     * @param generator the {@link DerivePath} generator to use for deriving the path.
     * @param pathForBinding the {@link Path} to use for binding in the derivation process.
     * @return the derived {@link Path}.
     * @throws DerivePathException if an error occurs during path derivation.
     */
    public static Path derivePath(DerivePath generator, Path pathForBinding)
            throws DerivePathException {
        return generator.deriveFrom(pathForBinding, DEBUG_MODE_NON_INPUT);
    }
}
