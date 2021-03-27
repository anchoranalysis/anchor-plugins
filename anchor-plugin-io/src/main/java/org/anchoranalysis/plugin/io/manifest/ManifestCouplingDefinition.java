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
import org.anchoranalysis.core.format.NonImageFileFormat;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.serialize.DeserializationFailedException;
import org.anchoranalysis.core.system.path.PathDifferenceException;
import org.anchoranalysis.experiment.bean.task.Task;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.manifest.Manifest;
import org.anchoranalysis.io.manifest.deserializer.ManifestDeserializer;
import org.anchoranalysis.io.manifest.directory.MutableDirectory;
import org.anchoranalysis.io.manifest.finder.FinderExperimentFileDirectories;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Links image manifests to experimental manifests
public class ManifestCouplingDefinition implements InputFromManager {

    private static final String JOB_MANIFEST_FILENAME_TO_READ =
            NonImageFileFormat.SERIALIZED_BINARY.buildPath(Task.OUTPUT_MANIFEST);

    private List<CoupledManifests> listCoupledManifests = new ArrayList<>();
    private MultiMap mapExperimentalToImages = new MultiValueMap();

    private static Log log = LogFactory.getLog(ManifestCouplingDefinition.class);

    public int numberManifests() {
        return listCoupledManifests.size();
    }

    /**
     * Adds job-manifests that don't have corresponding experiment-manifests.
     *
     * @param jobFiles the files of the job manifests
     * @param manifestDeserializer the deserializer to use
     * @param logger the logger
     */
    public void addUncoupledJobs(
            Collection<File> jobFiles, ManifestDeserializer manifestDeserializer, Logger logger) {

        for (File file : jobFiles) {

            if (file.exists()) {
                DeserializedManifest deserializedManifest =
                        new DeserializedManifest(file, manifestDeserializer);
                listCoupledManifests.add(new CoupledManifests(deserializedManifest, 3, logger));
            } else {
                log.debug(String.format("File %s does not exist", file.getPath()));
            }
        }
    }

    public void addManifestExperimentFileSet(
            Collection<File> matchingFiles,
            ManifestDeserializer manifestDeserializer,
            Logger logger)
            throws DeserializationFailedException {

        for (File experimentFile : matchingFiles) {
            // We deserialize each experimental manifest

            Manifest experimentManifest = manifestDeserializer.deserializeManifest(experimentFile);

            // We look for all experimental files in the manifest
            FinderExperimentFileDirectories finderExperimentFileDirectories =
                    new FinderExperimentFileDirectories();
            if (!finderExperimentFileDirectories.doFind(experimentManifest)) {
                break;
            }

            // For each experiment folder, we look for a manifest
            for (MutableDirectory folderWrite : finderExperimentFileDirectories.getList()) {
                CoupledManifests coupledManifests =
                        readManifestsFromDirectory(
                                folderWrite, experimentManifest, manifestDeserializer, logger);
                listCoupledManifests.add(coupledManifests);
                mapExperimentalToImages.put(experimentManifest, coupledManifests);
            }
        }
    }

    public Iterator<CoupledManifests> iteratorCoupledManifestsFor(Manifest experimentalRecorder) {
        @SuppressWarnings("unchecked")
        List<CoupledManifests> list =
                (List<CoupledManifests>) mapExperimentalToImages.get(experimentalRecorder);
        return list.iterator();
    }

    public Iterator<CoupledManifests> iteratorCoupledManifests() {
        return listCoupledManifests.iterator();
    }

    @SuppressWarnings("unchecked")
    public Iterator<Manifest> iteratorExperimentalManifests() {
        return mapExperimentalToImages.keySet().iterator();
    }

    @Override
    public String identifier() {
        return "manifestCouplingDefinition";
    }

    @Override
    public Optional<Path> pathForBinding() {
        return Optional.empty();
    }

    private CoupledManifests readManifestsFromDirectory(
            MutableDirectory folderWrite,
            Manifest manifestExperimentRecorder,
            ManifestDeserializer manifestDeserializer,
            Logger logger)
            throws DeserializationFailedException {

        DeserializedManifest manifestExperiment =
                new DeserializedManifest(experimentManifestFile(folderWrite), manifestDeserializer);
        try {
            return new CoupledManifests(manifestExperimentRecorder, manifestExperiment, logger);
        } catch (PathDifferenceException e) {
            throw new DeserializationFailedException(e);
        }
    }

    private static File experimentManifestFile(MutableDirectory folderWrite) {
        return new File(
                String.format(
                        "%s%s%s",
                        folderWrite.calculatePath(),
                        File.separator,
                        JOB_MANIFEST_FILENAME_TO_READ));
    }
}
