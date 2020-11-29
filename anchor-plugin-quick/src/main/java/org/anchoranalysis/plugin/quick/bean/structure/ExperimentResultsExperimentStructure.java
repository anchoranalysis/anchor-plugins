/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.plugin.quick.bean.structure;

import java.io.File;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithDirectory;
import org.anchoranalysis.io.input.bean.files.SearchDirectory;
import org.anchoranalysis.io.input.bean.path.matcher.MatchGlob;
import org.anchoranalysis.io.input.files.FilesProviderException;
import org.anchoranalysis.plugin.io.bean.provider.file.Rooted;

/**
 * Finds some files produced in a previous experiment assuming a certain structure
 *
 * <p>A convenience method for commonly used prefixer settings when the output occurs in an
 * experiment/$1/ filesystem structure where $1 is the experimentType
 */
public class ExperimentResultsExperimentStructure extends FilesProvider {

    // START BEAN PROPERTIES
    /** The name of the experiment including version suffix */
    @BeanField @Getter @Setter private String datasetName;

    /** A version-suffix appended to the dataset name extracted from the reg exp */
    @BeanField @Getter @Setter private String datasetVersion;

    /**
     * A folder identifying the type of experiment (where the outputs are all put in the same
     * directory
     */
    @BeanField @Getter @Setter private String experimentType;

    /** Root-name assuming multi-rooted strucutre */
    @BeanField @Getter @Setter private String rootName;

    /** Files to search for */
    @BeanField @Getter @Setter private String fileFilter = "*";

    /**
     * If true the directory structure is
     * ROOT/experiments/${ROOT_NAME}/${DATASET_NAME}_${DATASET_SUFFIX} If false, the directory
     * structure is ROOT/experiments/${DATASET_NAME}_${DATASET_SUFFIX}
     */
    @BeanField @Getter @Setter private boolean rootInStructure = false;

    /**
     * If rootInStructure==true, the rootName is placed in the folder structure. If this is
     * non-empty, a different rootName is inserted into this structure instead of the default
     * rootName
     */
    @BeanField @AllowEmpty @Getter @Setter private String rootNameForStructure = "";

    /**
     * If non-empty than an additional subdirectory is appended as a suffix
     * ${DATASET_NAME}_${DATASET_SUFFIX}/${SUBDIRECTORY}/
     */
    @BeanField @AllowEmpty @Getter @Setter private String subdirectory = "";

    /**
     * If true the datasetName is appended as a subdirectory e.g.
     * ${DATASET_NAME}_${DATASET_SUFFIX}/${DATASET_NAME}
     */
    @BeanField @Getter @Setter private boolean datasetNameSubdirectory = false;

    /** Whether to apply the search recursively or not */
    @BeanField @Getter @Setter private boolean recursive = false;

    @BeanField @Getter @Setter private int maxDirectoryDepth;
    // END BEAN PROPERTIES

    private Rooted delegate;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        delegate = createRootedFileSet();
        delegate.checkMisconfigured(defaultInstances);

        if (!subdirectory.isEmpty() && datasetNameSubdirectory) {
            throw new BeanMisconfiguredException(
                    "Both subdirectory and datasetNameSubdirectory should not be set");
        }
    }

    private Rooted createRootedFileSet() {
        Rooted out = new Rooted();
        out.setDisableDebugMode(true);
        out.setRootName(rootName);
        out.setFiles(createFiles());
        return out;
    }

    private FilesProviderWithDirectory createFiles() {
        SearchDirectory out = new SearchDirectory();
        out.setDirectory(new DirectoryCreator().apply());
        out.setMatcher(new MatchGlob(fileFilter));
        out.setRecursive(recursive);
        out.setMaxDirectoryDepth(maxDirectoryDepth);
        return out;
    }

    private class DirectoryCreator {

        public String apply() {
            StringBuilder sb = new StringBuilder("experiments/");

            if (rootInStructure) {
                addRootName(sb);
            }

            addExperimentType(sb);
            addDataset(sb);

            return sb.toString();
        }

        private void addRootName(StringBuilder sb) {
            if (!rootNameForStructure.isEmpty()) {
                sb.append(rootNameForStructure);
            } else {
                sb.append(rootName);
            }
            sb.append("/");
        }

        private void addExperimentType(StringBuilder sb) {
            sb.append(experimentType);
            sb.append("/");
        }

        private void addDataset(StringBuilder sb) {
            sb.append(datasetName);
            sb.append("_");
            sb.append(datasetVersion);
            sb.append("/");

            if (!subdirectory.isEmpty()) {
                sb.append(subdirectory);
                sb.append("/");
            }

            if (datasetNameSubdirectory) {
                sb.append(datasetName);
                sb.append("/");
            }
        }
    }

    @Override
    public Collection<File> create(InputManagerParams params) throws FilesProviderException {
        return delegate.create(params);
    }
}
