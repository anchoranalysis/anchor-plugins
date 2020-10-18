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

package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;

/**
 * Rejects files that fail to match a particular regular-expression
 *
 * @author Owen Feehan
 */
public class EnsureRegExMatch extends CopyFilesNamingOneRegEx {

    // START BEAN PROPERTIES
    /**
     * Iff true, then a file is rejected if regEx matches and vice-versa (opposite to normal
     * behaviour)
     */
    @BeanField @Getter @Setter private boolean invert = false;
    // END BEAN PROPERTIES

    @Override
    protected Optional<Path> destinationPathRelative(Path pathDelegate, String regex) {
        if (acceptPath(pathDelegate, regex)) {
            return Optional.of(pathDelegate);
        } else {
            return Optional.empty();
        }
    }

    private boolean acceptPath(Path path, String regex) {
        boolean matches = NamingUtilities.convertToString(path).matches(regex);

        if (invert) {
            return !matches;
        } else {
            return matches;
        }
    }
}
