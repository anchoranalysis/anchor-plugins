/* (C)2020 */
package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.cfg;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.io.generator.serialized.SerializedGenerator;
import org.anchoranalysis.io.manifest.ManifestDescription;
import org.anchoranalysis.io.output.bean.OutputWriteSettings;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

class GroupParamsGenerator extends SerializedGenerator {

    private KeyValueParams params;

    public GroupParamsGenerator(KeyValueParams params) {
        super();
        this.params = params;
    }

    @Override
    public void writeToFile(OutputWriteSettings outputWriteSettings, Path filePath)
            throws OutputWriteFailedException {
        try {
            params.writeToFile(filePath);
        } catch (IOException e) {
            throw new OutputWriteFailedException(e);
        }
    }

    @Override
    public String getFileExtension(OutputWriteSettings outputWriteSettings) {
        return "properties.xml";
    }

    @Override
    public Optional<ManifestDescription> createManifestDescription() {
        return Optional.of(new ManifestDescription("serialized", "groupParams"));
    }
}
