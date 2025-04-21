/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;

/**
 * Multiplexes between two namers depending on if the <i>relative-to-directory</i> option is
 * selected.
 *
 * @author Owen Feehan
 */
public class IfRelativeToDirectory extends FileNamer {

    // START BEAN PROPERTIES
    /** The namer to use if the <i>relative-to-directory</i> option <b>is selected</b>. */
    @BeanField @Getter @Setter private FileNamer whenRelative;

    /** The namer to use if the <i>relative-to-directory</i> option <b>is not selected</b>. */
    @BeanField @Getter @Setter private FileNamer otherwise;

    // END BEAN PROPERTIES

    @Override
    public List<NamedFile> deriveName(List<File> files, FileNamerContext context) {
        if (context.isRelativeToDirectory()) {
            return whenRelative.deriveName(files, context);
        } else {
            return otherwise.deriveName(files, context);
        }
    }
}
