/*-
 * #%L
 * anchor-io
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

package org.anchoranalysis.plugin.io.bean.input.files.provider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithDirectoryString;
import org.anchoranalysis.io.input.file.FilesProviderException;

/** Lists all directories to a certain depth */
public class DirectoryDepth extends FilesProviderWithDirectoryString {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int exactDepth = 0;

    // END BEAN PROPERTIES

    @Override
    public List<File> matchingFilesForDirectory(Path directory, InputManagerParameters parameters)
            throws FilesProviderException {

        String[] filesDirectory = directory.toFile().list();

        if (filesDirectory == null) {
            throw new FilesProviderException(
                    String.format("Path %s is not valid. Cannot enumerate directory.", directory));
        }

        WalkToDepth walkTo = new WalkToDepth(exactDepth);
        try {
            return walkTo.findDirs(directory.toFile());
        } catch (IOException e) {
            throw new FilesProviderException(e);
        }
    }
}
