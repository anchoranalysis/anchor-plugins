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
/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.io.error.AnchorIOException;

public abstract class CopyFilesNamingOne implements CopyFilesNaming {

    // START BEAN PROPERTIES
    @BeanField private CopyFilesNaming copyFilesNaming;
    // END BEAN PROPERTIES

    @Override
    public void beforeCopying(Path destDir, int totalNumFiles) {
        copyFilesNaming.beforeCopying(destDir, totalNumFiles);
    }

    @Override
    public Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int iter)
            throws AnchorIOException {
        Optional<Path> pathDelegate =
                copyFilesNaming.destinationPathRelative(sourceDir, destDir, file, iter);
        return OptionalUtilities.flatMap(pathDelegate, this::destinationPathRelative);
    }

    protected abstract Optional<Path> destinationPathRelative(Path pathDelegate)
            throws AnchorIOException;

    @Override
    public void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException {
        copyFilesNaming.afterCopying(destDir, dummyMode);
    }

    public CopyFilesNaming getCopyFilesNaming() {
        return copyFilesNaming;
    }

    public void setCopyFilesNaming(CopyFilesNaming copyFilesNaming) {
        this.copyFilesNaming = copyFilesNaming;
    }
}
