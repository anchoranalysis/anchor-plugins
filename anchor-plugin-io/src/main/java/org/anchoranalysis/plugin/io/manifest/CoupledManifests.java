/* (C)2020 */
package org.anchoranalysis.plugin.io.manifest;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.PathDifferenceFromBase;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.manifest.ManifestRecorder;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;

// A file manifest together with the overall manifest for the experiment
public class CoupledManifests implements InputFromManager {

    @Getter private final Optional<ManifestRecorder> experimentManifest;

    @Getter private final ManifestRecorderFile fileManifest;

    private final String name;

    public CoupledManifests(
            ManifestRecorder experimentManifest, ManifestRecorderFile fileManifest, Logger logger)
            throws AnchorIOException {
        super();
        this.experimentManifest = Optional.of(experimentManifest);
        this.fileManifest = fileManifest;
        name = generateName(logger);
    }

    public CoupledManifests(
            ManifestRecorderFile fileManifest, int numFoldersInDescription, Logger logger) {
        super();
        this.experimentManifest = Optional.empty();
        this.fileManifest = fileManifest;
        name = generateNameFromFolders(numFoldersInDescription, logger);
    }

    private String generateName(Logger logger) throws AnchorIOException {

        if (experimentManifest.isPresent()) {
            Path experimentRootFolder = getExperimentManifest().get().getRootFolder().calcPath();

            PathDifferenceFromBase ff =
                    PathDifferenceFromBase.differenceFrom(
                            experimentRootFolder, fileManifest.getRootPath());
            return ff.combined().toString();

        } else {
            return generateNameFromFolders(0, logger);
        }
    }

    private String generateNameFromFolders(int numFoldersInDescription, Logger logger) {
        LastFolders dnff = new LastFolders();
        dnff.setNumFoldersInDescription(numFoldersInDescription);
        dnff.setRemoveExtensionInDescription(false);
        return dnff.descriptiveNameFor(fileManifest.getRootPath().toFile(), "<unknown>", logger)
                .getDescriptiveName();
    }

    @Override
    public String descriptiveName() {
        return name;
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.of(fileManifest.getRootPath());
    }
}
