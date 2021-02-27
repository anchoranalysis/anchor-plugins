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

package org.anchoranalysis.plugin.quick.bean.input.filepathappend;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

public class AppendDictionary extends FilePathBaseAppendToManager {

    private static final String DICTIONARY_FILENAME_WITHOUT_EXTENSION = "dictionary";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean includeFileName = false;

    // If non-empty this directory is appended to make the out path string
    @BeanField @AllowEmpty @Getter @Setter private String subdirectory = "";
    // END BEAN PROPERTIES

    @Override
    protected List<NamedBean<DerivePath>> getListFromManager(MultiInputManager inputManager)
            throws BeanMisconfiguredException {
        return inputManager.getAppendDictionary();
    }

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {
        String firstPart = selectMaybeDir(selectFirstPart());
        return NonImageFileFormat.XML.buildPath(firstPart, DICTIONARY_FILENAME_WITHOUT_EXTENSION);
    }

    private String selectMaybeDir(String firstPart) {

        if (!subdirectory.isEmpty()) {
            return String.format("%s/%s", firstPart, subdirectory);
        } else {
            return firstPart;
        }
    }

    private String selectFirstPart() {
        if (includeFileName) {
            return firstPartWithFilename();
        } else {
            return firstPart();
        }
    }
}
