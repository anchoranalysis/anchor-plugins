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

package org.anchoranalysis.plugin.io.bean.input.manifest;

import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.file.FilesProviderException;
import org.anchoranalysis.io.manifest.deserializer.ManifestDeserializer;
import org.anchoranalysis.io.manifest.deserializer.SimpleManifestDeserializer;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;

public class CoupledManifestsInputManager extends InputManager<ManifestCouplingDefinition> {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private FilesProvider manifestInputFileSet;

    @BeanField @OptionalBean @Getter @Setter private FilesProvider manifestExperimentInputFileSet;

    @BeanField @Getter @Setter private ManifestDeserializer manifestDeserializer;
    // END BEAN PROPERTIES

    private ManifestCouplingDefinition manifestCouplingDefinition = null;

    public CoupledManifestsInputManager() {
        manifestDeserializer = new CachedManifestDeserializer(new SimpleManifestDeserializer(), 50);
    }

    @Override
    public InputsWithDirectory<ManifestCouplingDefinition> inputs(InputManagerParams params)
            throws InputReadFailedException {

        try {
            if (manifestCouplingDefinition == null) {
                manifestCouplingDefinition = createDeserializedList(params);
            }
        } catch (DeserializationFailedException e) {
            throw new InputReadFailedException("Deserialization failed", e);
        }

        return new InputsWithDirectory<>(Collections.singletonList(manifestCouplingDefinition));
    }

    public ManifestCouplingDefinition manifestCouplingDefinition(InputManagerParams params)
            throws DeserializationFailedException {
        if (manifestCouplingDefinition == null) {
            manifestCouplingDefinition = createDeserializedList(params);
        }
        return manifestCouplingDefinition;
    }

    // Runs the experiment on a particular file
    private ManifestCouplingDefinition createDeserializedList(InputManagerParams params)
            throws DeserializationFailedException {

        try {
            ManifestCouplingDefinition definition = new ManifestCouplingDefinition();

            // Uncoupled file manifests
            if (manifestInputFileSet != null) {
                definition.addUncoupledJobs(
                        manifestInputFileSet.create(params),
                        manifestDeserializer,
                        params.getLogger());
            }

            if (manifestExperimentInputFileSet != null) {
                definition.addManifestExperimentFileSet(
                        manifestExperimentInputFileSet.create(params),
                        manifestDeserializer,
                        params.getLogger());
            }

            return definition;
        } catch (FilesProviderException e) {
            throw new DeserializationFailedException("Cannot find files to deserialize", e);
        }
    }

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (manifestInputFileSet == null && manifestExperimentInputFileSet == null) {
            throw new BeanMisconfiguredException(
                    "Either the manifestInputList must be populated or manifestInputFileSet must be set or the manifestExperimentInputFileSet must be set");
        }
    }
}
