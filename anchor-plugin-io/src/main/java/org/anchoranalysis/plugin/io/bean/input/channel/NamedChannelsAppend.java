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

package org.anchoranalysis.plugin.io.bean.input.channel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.functional.FunctionalProgress;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.input.NamedChannelsInputPart;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.path.derive.DerivePath;
import org.anchoranalysis.io.exception.DerivePathException;
import org.anchoranalysis.io.exception.InputReadFailedException;
import org.anchoranalysis.io.input.OperationOutFilePath;
import org.anchoranalysis.io.input.PathSupplier;

public class NamedChannelsAppend extends NamedChannelsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<NamedChannelsInputPart> input;

    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReader;

    @BeanField @OptionalBean @Getter @Setter private List<NamedBean<DerivePath>> listAppend;

    @BeanField @Getter @Setter private boolean forceEagerEvaluation = false;

    @BeanField @Getter @Setter private boolean ignoreFileNotFoundAppend = false;

    @BeanField @Getter @Setter private boolean skipMissingChannels = false;
    // END BEAN PROPERTIES

    @Override
    public List<NamedChannelsInputPart> inputs(InputManagerParams params) throws InputReadFailedException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), 2)) {

            Iterator<NamedChannelsInputPart> itr = input.inputs(params).iterator();

            prm.incrWorker();

            List<NamedChannelsInputPart> listTemp = new ArrayList<>();
            while (itr.hasNext()) {
                listTemp.add(itr.next());
            }

            List<NamedChannelsInputPart> outList =
                    createOutList(
                            listTemp,
                            new ProgressReporterOneOfMany(prm),
                            params.isDebugModeActivated());

            prm.incrWorker();

            return outList;
        }
    }

    private List<NamedChannelsInputPart> createOutList(
            List<NamedChannelsInputPart> listTemp,
            ProgressReporter progressReporter,
            boolean debugMode)
            throws InputReadFailedException {
        try {
            return FunctionalProgress.mapListOptional(
                    listTemp, progressReporter, ncc -> maybeAppend(ncc, debugMode));
        } catch (DerivePathException e) {
            throw new InputReadFailedException(e);
        }
    }

    private Optional<NamedChannelsInputPart> maybeAppend(
            final NamedChannelsInputPart ncc, boolean debugMode) throws DerivePathException {
        if (ignoreFileNotFoundAppend) {

            try {
                return Optional.of(append(ncc, debugMode));
            } catch (DerivePathException e) {
                return Optional.empty();
            }

        } else {
            return Optional.of(append(ncc, debugMode));
        }
    }

    // We assume all the input files are single channel images
    private NamedChannelsInputPart append(final NamedChannelsInputPart ncc, boolean debugMode)
            throws DerivePathException {

        NamedChannelsInputPart out = ncc;

        if (listAppend == null) {
            return out;
        }

        for (final NamedBean<DerivePath> ni : listAppend) {

            // Delayed-calculation of the appending path as it can be a bit expensive when
            // multiplied by so many items
            PathSupplier outPath =
                    OperationOutFilePath.cachedOutPathFor(
                            ni.getValue(), ncc::pathForBinding, debugMode);

            if (forceEagerEvaluation) {
                Path path = outPath.get();
                if (!path.toFile().exists()) {

                    if (skipMissingChannels) {
                        continue;
                    } else {
                        throw new DerivePathException(
                                String.format("Append path: %s does not exist", path));
                    }
                }
            }

            out = new AppendPart(out, ni.getName(), 0, outPath, stackReader);
        }

        return out;
    }
}
