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
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.cache.CachedSupplier;
import org.anchoranalysis.core.functional.FunctionalProgress;
import org.anchoranalysis.core.progress.Progress;
import org.anchoranalysis.core.progress.ProgressMultiple;
import org.anchoranalysis.core.progress.ProgressOneOfMany;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInputPart;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.io.input.path.DerivePathException;
import org.anchoranalysis.io.input.path.PathSupplier;

public class NamedChannelsAppend extends NamedChannelsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private InputManager<NamedChannelsInputPart> input;

    @BeanField @OptionalBean @Getter @Setter private List<NamedBean<DerivePath>> listAppend;

    @BeanField @Getter @Setter private boolean forceEagerEvaluation = false;

    @BeanField @Getter @Setter private boolean ignoreFileNotFoundAppend = false;

    @BeanField @Getter @Setter private boolean skipMissingChannels = false;
    // END BEAN PROPERTIES

    @Override
    public InputsWithDirectory<NamedChannelsInputPart> inputs(InputManagerParams params)
            throws InputReadFailedException {

        try (ProgressMultiple progressMultiple = new ProgressMultiple(params.getProgress(), 2)) {

            InputsWithDirectory<NamedChannelsInputPart> inputs = input.inputs(params);

            Iterator<NamedChannelsInputPart> iterator = inputs.iterator();

            progressMultiple.incrementWorker();

            List<NamedChannelsInputPart> listTemp = new ArrayList<>();
            while (iterator.hasNext()) {
                listTemp.add(iterator.next());
            }

            List<NamedChannelsInputPart> outList =
                    createOutList(
                            listTemp,
                            new ProgressOneOfMany(progressMultiple),
                            params.isDebugModeActivated());

            progressMultiple.incrementWorker();

            return inputs.withInputs(outList);
        }
    }

    private List<NamedChannelsInputPart> createOutList(
            List<NamedChannelsInputPart> listTemp, Progress progress, boolean debugMode)
            throws InputReadFailedException {
        try {
            return FunctionalProgress.mapListOptional(
                    listTemp, progress, channels -> maybeAppend(channels, debugMode));
        } catch (DerivePathException e) {
            throw new InputReadFailedException(e);
        }
    }

    private Optional<NamedChannelsInputPart> maybeAppend(
            final NamedChannelsInputPart channels, boolean debugMode) throws DerivePathException {
        if (ignoreFileNotFoundAppend) {

            try {
                return Optional.of(append(channels, debugMode));
            } catch (DerivePathException e) {
                return Optional.empty();
            }

        } else {
            return Optional.of(append(channels, debugMode));
        }
    }

    // We assume all the input files are single channel images
    private NamedChannelsInputPart append(final NamedChannelsInputPart ncc, boolean debugMode)
            throws DerivePathException {

        NamedChannelsInputPart out = ncc;

        if (listAppend == null) {
            return out;
        }

        for (NamedBean<DerivePath> namedPath : listAppend) {

            // Delayed-calculation of the appending path as it can be a bit expensive when
            // multiplied by so many items
            PathSupplier outPath =
                    cachedOutPathFor(namedPath.getValue(), ncc::pathForBinding, debugMode);

            if (forceEagerEvaluation && !skipMissingChannels) {
                Path path = outPath.get();
                if (!path.toFile().exists()) {
                    throw new DerivePathException(
                            String.format("Append path: %s does not exist", path));
                }
            }

            out = new AppendPart(out, namedPath.getName(), 0, outPath, getStackReader());
        }

        return out;
    }

    private static PathSupplier cachedOutPathFor(
            DerivePath outputPathGenerator, Supplier<Optional<Path>> pathInput, boolean debugMode) {
        return cachePathSupplier(() -> outputPathGenerator.deriveFrom(pathInput, debugMode));
    }

    private static PathSupplier cachePathSupplier(PathSupplier supplier) {
        return CachedSupplier.cache(supplier::get)::get;
    }
}
