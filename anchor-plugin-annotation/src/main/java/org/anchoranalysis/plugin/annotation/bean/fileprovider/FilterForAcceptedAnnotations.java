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

package org.anchoranalysis.plugin.annotation.bean.fileprovider;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.mark.MarkAnnotationReader;
import org.anchoranalysis.annotation.mark.MarkAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;

public class FilterForAcceptedAnnotations extends FileProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FileProvider fileProvider;

    @BeanField @Getter @Setter
    private List<FilePathGenerator> listFilePathGenerator = new ArrayList<>();
    // END BEAN PROPERTIES

    private MarkAnnotationReader annotationReader = new MarkAnnotationReader(false);

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {
        try {
            Collection<File> filesIn = fileProvider.create(params);

            List<File> filesOut = new ArrayList<>();

            for (File f : filesIn) {

                if (isFileAccepted(f)) {
                    filesOut.add(f);
                }
            }

            return filesOut;
        } catch (AnchorIOException e) {
            throw new FileProviderException(e);
        }
    }

    private boolean isFileAccepted(File file) throws AnchorIOException {

        for (FilePathGenerator fpg : listFilePathGenerator) {
            Path annotationPath = fpg.outFilePath(file.toPath(), false);

            Optional<MarkAnnotation> annotation = annotationReader.read(annotationPath);

            if (!annotation.isPresent() || !annotation.get().isAccepted()) {
                return false;
            }
        }
        return true;
    }
}
