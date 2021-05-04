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
package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.io.output.bean.OutputManager;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.input.path.CopyContext;

/**
 * Copies files using whatever prefix is assigned to an input by the {@link OutputManager} as the
 * file-name, adding the same extension as the source file.
 *
 * <p>An extension is added, based on whatever extension already exists on the file.
 *
 * <p>The prefix must be guaranteed to be unique for each file copied. This is not actively checked.
 *
 * <p>The prefix is usually derived from the name of the input file relative to the input-directory,
 * so behavior is often similar to {@link PreserveName}. However, other naming-schemes are also
 * possible e.g. assigning a sequence of numbers to each input.
 *
 * @author Owen Feehan
 */
public class OutputPrefixWithExtension extends CopyFilesNamingWithoutSharedState {

    @Override
    public Optional<Path> destinationPathRelative(
            File file,
            DirectoryWithPrefix outputTarget,
            int index,
            CopyContext<NoSharedState> context)
            throws OutputWriteFailedException {

        Optional<String> extension = ExtensionUtilities.extractExtension(file.toString());

        Path prefixRelativeToSource =
                context.getDestinationDirectory().relativize(outputTarget.asPath(false));

        return Optional.of(ExtensionUtilities.appendExtension(prefixRelativeToSource, extension));
    }
}
