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

package org.anchoranalysis.plugin.quick.bean.input.filepathappend;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.bean.shared.regex.RegExSimple;
import org.anchoranalysis.io.input.bean.path.DerivePath;
import org.anchoranalysis.plugin.io.bean.path.derive.InsertRegExGroups;
import org.anchoranalysis.plugin.io.bean.path.derive.Rooted;
import org.anchoranalysis.plugin.quick.bean.file.path.derive.CollapseFileName;
import org.anchoranalysis.plugin.quick.bean.file.path.derive.RemoveTrailingDirectory;

public abstract class FilePathAppendBase extends AnchorBean<FilePathAppendBase> {

    // START BEAN FIELDS
    /** The name of the appended entity */
    @BeanField @Getter @Setter private String name;

    /** A suffix appended to the dataset name extracted from the reg exp */
    @BeanField @Getter @Setter private String datasetSuffix;

    /**
     * A folder identifying the type of experiment (where the outputs are all put in the same
     * directory
     */
    @BeanField @Getter @Setter private String experimentType;

    // If non-zero, n trailing directories are removed from the end
    @BeanField @Getter @Setter private int trimTrailingDirectory = 0;

    @BeanField @Getter @Setter private int skipFirstTrim = 0;

    // Do not include the file-name
    @BeanField @Getter @Setter private boolean skipFileName = false;

    // Iff true, then the filename will be removed apart from the .extension which remains
    //  Specifically path/name.ext becomes path.ext
    @BeanField @Getter @Setter private boolean collapseFilename = false;
    // END BEAN FIELDS

    protected abstract String createOutPathString() throws BeanMisconfiguredException;

    /*** The first-part of the out-path string including the file-name of the previous experiment as a folder */
    protected String firstPartWithFilename() {
        return firstPartWithCustomEnd("/$3");
    }

    /*** The first-part of the out-path string including the dataset of the current experiment as a folder */
    protected String firstPartWithDataset() {
        return firstPartWithCustomEnd("/$2");
    }

    protected String firstPart() {
        return firstPartWithCustomEnd("");
    }

    private String firstPartWithCustomEnd(String end) {
        return String.format(
                "$1/experiments/%s/$2%s%s", getExperimentType(), getDatasetSuffix(), end);
    }

    protected String firstPartWithCustomMiddle(String middle) {
        return String.format(
                "$1/experiments/%s/$2%s%s/$2", getExperimentType(), getDatasetSuffix(), middle);
    }

    /**
     * Creates a (rooted) file-path generator for a rootName and a regEx which matches three groups
     * The first group should correspond to top-level folder for the project The second group should
     * correspond to the unique name of the dataset. The third group should correspond to the unique
     * name of the experiment.
     *
     * @param rootName if non-empty (and non-null) a rooted derivePath is created instead of a non
     *     rooted
     * @param regEx
     * @return
     * @throws BeanMisconfiguredException
     */
    public NamedBean<DerivePath> createPathDeriver(String rootName, String regEx)
            throws BeanMisconfiguredException {

        DerivePath derivePath = createRegEx(regEx);

        if (rootName != null && !rootName.isEmpty()) {
            derivePath = addRoot(derivePath, rootName);
        }

        if (trimTrailingDirectory > 0 || skipFileName) {
            derivePath = addRemoveTrailing(derivePath);
        }

        if (collapseFilename) {
            derivePath = addCollapse(derivePath);
        }

        // Wrap in a named bean
        return new NamedBean<>(name, derivePath);
    }

    private DerivePath addCollapse(DerivePath deriver) {
        CollapseFileName collapse = new CollapseFileName();
        collapse.setDerivePath(deriver);
        return collapse;
    }

    private DerivePath addRemoveTrailing(DerivePath deriver) {
        RemoveTrailingDirectory remove = new RemoveTrailingDirectory();
        remove.setDerivePath(deriver);
        remove.setTrimTrailingDirectory(trimTrailingDirectory);
        remove.setSkipFirstTrim(skipFirstTrim);
        return remove;
    }

    private static DerivePath addRoot(DerivePath deriver, String rootName) {
        // Rooted File-Path
        Rooted delegate = new Rooted();
        delegate.setRootName(rootName);
        delegate.setItem(deriver);
        return delegate;
    }

    private DerivePath createRegEx(String regEx) throws BeanMisconfiguredException {
        return new InsertRegExGroups(new RegExSimple(regEx), createOutPathString());
    }
}
