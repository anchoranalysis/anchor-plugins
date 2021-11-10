/*-
 * #%L
 * anchor-io-manifest
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

package org.anchoranalysis.plugin.io.manifest;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.core.cache.CachedSupplier;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedSupplier;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.io.manifest.Manifest;
import org.anchoranalysis.io.manifest.deserializer.ManifestDeserializer;

/**
 * A manifest after being deserialized from the file-system.
 *
 * <p>It performs caching and providers other utility functions.
 *
 * <p>It is <b>not</b> the main data object-used in writing the manifest, rather see {@link
 * Manifest}.
 *
 * @author Owen Feehan
 */
public class DeserializedManifest {

    private final File file;
    private final CheckedSupplier<Manifest, OperationFailedException> memoized;
    private Logger logger;

    public DeserializedManifest(File file, ManifestDeserializer manifestDeserializer, Logger logger) {
        this.file = file;
        this.logger = logger;
        this.memoized = CachedSupplier.cache(() -> getInternal(manifestDeserializer));
    }

    public Manifest get() throws OperationFailedException {
        return memoized.get();
    }

    public Path getRootPath() {
        // Returns the path of the root of the manifest file (or what it will become)
        return Paths.get(file.getParent());
    }

    private Manifest getInternal(ManifestDeserializer manifestDeserializer)
            throws OperationFailedException {
        try {
            if (!file.exists()) {
                throw new OperationFailedException(
                        String.format("File %s cannot be found", file.getPath()));
            }
            return manifestDeserializer.deserializeManifest(file, logger);
        } catch (DeserializationFailedException e) {
            throw new OperationFailedException(e);
        }
    }
}
