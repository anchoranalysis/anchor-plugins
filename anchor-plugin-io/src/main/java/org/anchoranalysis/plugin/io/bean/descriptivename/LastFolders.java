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

package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFileIndependent;
import org.apache.commons.io.FilenameUtils;

@NoArgsConstructor
public class LastFolders extends DescriptiveNameFromFileIndependent {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int numFoldersInDescription = 0;

    @BeanField @Getter @Setter private boolean removeExtensionInDescription = true;

    @BeanField @Getter @Setter private int skipFirstNumFolders = 0;

    @BeanField @Getter @Setter private boolean skipFileName = false;
    // END BEAN PROPERTIES

    public LastFolders(int numFoldersInDescription) {
        this.numFoldersInDescription = numFoldersInDescription;
    }

    @Override
    protected String createDescriptiveName(File file, int index) {

        String nameOut = "";
        if (!skipFileName) {
            nameOut = maybeRemoveExtension(new File(file.getPath()).getName());
        }

        File currentFile = file;
        for (int i = 0; i < numFoldersInDescription; i++) {

            currentFile = currentFile.getParentFile();

            if (currentFile == null) {
                break;
            }

            if (i >= skipFirstNumFolders) {
                nameOut = prependMaybeWithSlash(currentFile.getName(), nameOut);
            }
        }
        return nameOut;
    }

    private String prependMaybeWithSlash(String prepend, String existing) {
        if (existing.isEmpty()) {
            return prepend;
        } else {
            return prepend + "/" + existing;
        }
    }

    private String maybeRemoveExtension(String path) {
        if (removeExtensionInDescription) {
            return FilenameUtils.removeExtension(path);
        } else {
            return path;
        }
    }
}
