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

package org.anchoranalysis.plugin.io.bean.file.namer;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.io.input.bean.namer.FileNamerIndependent;

/**
 * Derives a name by taking the filename together with optionally several subdirectories names.
 *
 * <p>The subdirectory names to use are limited by a certain number, working backwards from the
 * filename to the file-system root.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class LastDirectories extends FileNamerIndependent {

    // START BEAN PROPERTIES
    /**
     * How many directories to include in the name.
     *
     * <p>This refers to the last-most sub-directories i.e. working backwards from the filename to
     * the file-system root.
     */
    @BeanField @Getter @Setter private int numberSubdirectories = 0;

    /** Whether to remove the file-extension from the filename. */
    @BeanField @Getter @Setter private boolean removeExtensionInDescription = true;

    /**
     * Initially skip this number of directories before including them in the description.
     *
     * <p>This refers to the last-most directories i.e. working backwards from the filename to the
     * file-system root.
     */
    @BeanField @Getter @Setter private int skipNumberSubdirectories = 0;

    /** Iff true the filename is not considered, only the subdirectories. */
    @BeanField @Getter @Setter private boolean skipFileName = false;

    // END BEAN PROPERTIES

    /**
     * Creates for a particular number of subdirectories.
     *
     * @param numberSubdirectories the number of subdirectories
     */
    public LastDirectories(int numberSubdirectories) {
        this.numberSubdirectories = numberSubdirectories;
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (skipFileName && numberSubdirectories <= 0) {
            throw new BeanMisconfiguredException(
                    "If skipFileName is true, numberSubdirectories must be greater than 0");
        }
    }

    @Override
    protected String deriveName(File file, Optional<Path> inputDirectory, int index) {

        String nameOut = "";
        if (!skipFileName) {
            nameOut = maybeRemoveExtension(file);
        }

        File currentFile = file;
        for (int i = 0; i < numberSubdirectories; i++) {

            currentFile = currentFile.getParentFile();

            if (currentFile == null) {
                break;
            }

            if (i >= skipNumberSubdirectories) {
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

    private String maybeRemoveExtension(File file) {
        if (removeExtensionInDescription) {
            return ExtensionUtilities.filenameWithoutExtension(file);
        } else {
            return file.getName();
        }
    }
}
