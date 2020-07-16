/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.manifest;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.manifest.deserializer.CachedManifestDeserializer;
import org.anchoranalysis.io.manifest.deserializer.ManifestDeserializer;
import org.anchoranalysis.io.manifest.deserializer.SimpleManifestDeserializer;
import org.anchoranalysis.plugin.io.manifest.ManifestCouplingDefinition;

public class CoupledManifestsInputManager extends InputManager<ManifestCouplingDefinition> {

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private FileProvider manifestInputFileSet;

    @BeanField @OptionalBean @Getter @Setter private FileProvider manifestExperimentInputFileSet;

    @BeanField @Getter @Setter private ManifestDeserializer manifestDeserializer;
    // END BEAN PROPERTIES

    private ManifestCouplingDefinition mcd = null;

    public CoupledManifestsInputManager() {
        manifestDeserializer = new CachedManifestDeserializer(new SimpleManifestDeserializer(), 50);
    }

    @Override
    public List<ManifestCouplingDefinition> inputObjects(InputManagerParams params)
            throws AnchorIOException {

        try {
            if (mcd == null) {
                mcd = createDeserializedList(params);
            }
        } catch (DeserializationFailedException e) {
            throw new AnchorIOException("Deserialization failed", e);
        }

        return Collections.singletonList(mcd);
    }

    public ManifestCouplingDefinition manifestCouplingDefinition(InputManagerParams params)
            throws DeserializationFailedException {
        if (mcd == null) {
            mcd = createDeserializedList(params);
        }
        return mcd;
    }

    // Runs the experiment on a particular file
    private ManifestCouplingDefinition createDeserializedList(InputManagerParams params)
            throws DeserializationFailedException {

        try {
            ManifestCouplingDefinition definition = new ManifestCouplingDefinition();

            // Uncoupled file manifests
            if (manifestInputFileSet != null) {
                definition.addUncoupledFiles(
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
        } catch (FileProviderException e) {
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
