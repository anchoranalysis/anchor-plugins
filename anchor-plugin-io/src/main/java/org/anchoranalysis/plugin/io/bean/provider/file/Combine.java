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

package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.files.FilesProviderException;

public class Combine extends FilesProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private List<FilesProvider> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public Collection<File> create(InputManagerParams params) throws FilesProviderException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), list.size())) {

            List<File> combined = new ArrayList<>();

            for (FilesProvider fp : list) {

                ProgressReporterOneOfMany prLocal = new ProgressReporterOneOfMany(prm);
                combined.addAll(fp.create(params.withProgressReporter(prLocal)));
                prm.incrWorker();
            }
            return combined;
        }
    }
}