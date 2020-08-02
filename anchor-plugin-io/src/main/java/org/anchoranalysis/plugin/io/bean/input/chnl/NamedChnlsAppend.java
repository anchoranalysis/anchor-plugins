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

package org.anchoranalysis.plugin.io.bean.input.chnl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.cache.CachedOperation;
import org.anchoranalysis.core.functional.FunctionalProgress;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.OperationOutFilePath;
import lombok.Getter;
import lombok.Setter;

public class NamedChnlsAppend extends NamedChnlsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<NamedChnlsInputPart> input;

    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    @BeanField @OptionalBean @Getter @Setter private List<NamedBean<FilePathGenerator>> listAppend;

    @BeanField @Getter @Setter private boolean forceEagerEvaluation = false;

    @BeanField @Getter @Setter private boolean ignoreFileNotFoundAppend = false;

    @BeanField @Getter @Setter private boolean skipMissingChannels = false;
    // END BEAN PROPERTIES

    @Override
    public List<NamedChnlsInputPart> inputObjects(InputManagerParams params)
            throws AnchorIOException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), 2)) {

            Iterator<NamedChnlsInputPart> itr = input.inputObjects(params).iterator();

            prm.incrWorker();

            List<NamedChnlsInputPart> listTemp = new ArrayList<>();
            while (itr.hasNext()) {
                listTemp.add(itr.next());
            }

            List<NamedChnlsInputPart> outList =
                    createOutList(
                            listTemp,
                            new ProgressReporterOneOfMany(prm),
                            params.isDebugModeActivated());

            prm.incrWorker();

            return outList;
        }
    }

    private List<NamedChnlsInputPart> createOutList(
            List<NamedChnlsInputPart> listTemp,
            ProgressReporter progressReporter,
            boolean debugMode)
            throws AnchorIOException {
        return FunctionalProgress.mapListOptional(
                listTemp, progressReporter, ncc -> maybeAppend(ncc, debugMode));
    }

    private Optional<NamedChnlsInputPart> maybeAppend(
            final NamedChnlsInputPart ncc, boolean debugMode) throws AnchorIOException {
        if (ignoreFileNotFoundAppend) {

            try {
                return Optional.of(append(ncc, debugMode));
            } catch (AnchorIOException e) {
                return Optional.empty();
            }

        } else {
            return Optional.of(append(ncc, debugMode));
        }
    }

    // We assume all the input files are single channel images
    private NamedChnlsInputPart append(final NamedChnlsInputPart ncc, boolean debugMode)
            throws AnchorIOException {

        NamedChnlsInputPart out = ncc;

        if (listAppend == null) {
            return out;
        }

        for (final NamedBean<FilePathGenerator> ni : listAppend) {

            // Delayed-calculation of the appending path as it can be a bit expensive when
            // multiplied by so many items
            CachedOperation<Path, AnchorIOException> outPath = CachedOperation.of(
                    new OperationOutFilePath(ni, ncc::pathForBinding, debugMode)
            );

            if (forceEagerEvaluation) {
                Path path = outPath.call();
                if (!path.toFile().exists()) {

                    if (skipMissingChannels) {
                        continue;
                    } else {
                        throw new AnchorIOException(
                                String.format("Append path: %s does not exist", path));
                    }
                }
            }

            out = new AppendPart(out, ni.getName(), 0, outPath, rasterReader);
        }

        return out;
    }
}
