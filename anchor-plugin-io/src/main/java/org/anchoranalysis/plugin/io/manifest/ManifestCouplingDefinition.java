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

package org.anchoranalysis.plugin.io.manifest;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.manifest.ManifestRecorder;
import org.anchoranalysis.io.manifest.ManifestRecorderFile;
import org.anchoranalysis.io.manifest.deserializer.ManifestDeserializer;
import org.anchoranalysis.io.manifest.finder.FinderExperimentFileFolders;
import org.anchoranalysis.io.manifest.folder.FolderWrite;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Links image manifests to experimental manifests
public class ManifestCouplingDefinition implements InputFromManager {

    private List<CoupledManifests> listCoupledManifests = new ArrayList<>();
    private MultiMap mapExperimentalToImages = new MultiValueMap();

    private static Log log = LogFactory.getLog(ManifestCouplingDefinition.class);

    public int numManifests() {
        return listCoupledManifests.size();
    }

    public void addUncoupledFiles(
            Collection<File> allFiles, ManifestDeserializer manifestDeserializer, Logger logger) {

        for (File file : allFiles) {

            if (!file.exists()) {
                log.debug(String.format("File %s does not exist", file.getPath()));
                continue;
            }

            ManifestRecorderFile manifestRecorder =
                    new ManifestRecorderFile(file, manifestDeserializer);
            listCoupledManifests.add(new CoupledManifests(manifestRecorder, 3, logger));
        }
    }

    public void addManifestExperimentFileSet(
            Collection<File> matchingFiles,
            ManifestDeserializer manifestDeserializer,
            Logger logger)
            throws DeserializationFailedException {

        for (File experimentFile : matchingFiles) {
            // We deserialize each experimental manifest

            ManifestRecorder manifestExperimentRecorder =
                    manifestDeserializer.deserializeManifest(experimentFile);

            // We look for all experimental files in the manifest
            FinderExperimentFileFolders finderExperimentFileFolders =
                    new FinderExperimentFileFolders();
            if (!finderExperimentFileFolders.doFind(manifestExperimentRecorder)) {
                break;
            }

            // For each experiment folder, we look for a manifest
            for (FolderWrite folderWrite : finderExperimentFileFolders.getList()) {

                File file = fileForFolder(folderWrite);
                ManifestRecorderFile manifestRecorderFile =
                        new ManifestRecorderFile(file, manifestDeserializer);
                CoupledManifests cm;
                try {
                    cm =
                            new CoupledManifests(
                                    manifestExperimentRecorder, manifestRecorderFile, logger);
                } catch (AnchorIOException e) {
                    throw new DeserializationFailedException(e);
                }
                listCoupledManifests.add(cm);
                mapExperimentalToImages.put(manifestExperimentRecorder, cm);
            }
        }
    }

    private static File fileForFolder(FolderWrite folderWrite) {
        return new File(
                String.format("%s%s%s", folderWrite.calcPath(), File.separator, "manifest.ser"));
    }

    public Iterator<CoupledManifests> iteratorCoupledManifestsFor(
            ManifestRecorder experimentalRecorder) {
        @SuppressWarnings("unchecked")
        List<CoupledManifests> list =
                (List<CoupledManifests>) mapExperimentalToImages.get(experimentalRecorder);
        return list.iterator();
    }

    public Iterator<CoupledManifests> iteratorCoupledManifests() {
        return listCoupledManifests.iterator();
    }

    @SuppressWarnings("unchecked")
    public Iterator<ManifestRecorder> iteratorExperimentalManifests() {
        return mapExperimentalToImages.keySet().iterator();
    }

    @Override
    public String descriptiveName() {
        return "manifestCouplingDefinition";
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.empty();
    }
}
