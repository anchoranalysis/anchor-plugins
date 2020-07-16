/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.structure;

import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.FilePathPrefixerAvoidResolve;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.PathRegEx;
import org.anchoranalysis.plugin.io.bean.filepath.prefixer.Rooted;

/**
 * A file path prefixer that combines a prefix with an experimentType
 *
 * <p>A convenience method for commonly used prefixer settings when the output occurs in an
 * experiment/$1/ file-system structure where $1 is the experimentType
 */
public class FilePathPrefixerExperimentStructure extends FilePathPrefixer {

    // START BEAN PROPERTIES
    @BeanField private String experimentType;

    @BeanField private String rootName;

    @BeanField private RegEx regEx;

    @BeanField private String prefix;
    // END BEAN PROPERTIES

    private FilePathPrefixer delegate;

    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    public FilePathPrefix outFilePrefix(
            PathWithDescription input, String experimentIdentifier, FilePathPrefixerParams context)
            throws FilePathPrefixerException {

        createDelegateIfNeeded();

        return delegate.outFilePrefix(input, experimentIdentifier, context);
    }

    @Override
    public FilePathPrefix rootFolderPrefix(
            String experimentIdentifier, FilePathPrefixerParams context)
            throws FilePathPrefixerException {

        createDelegateIfNeeded();

        return delegate.rootFolderPrefix(experimentIdentifier, context);
    }

    private void createDelegateIfNeeded() throws FilePathPrefixerException {

        if (delegate != null) {
            // Nothing to do
            return;
        }

        this.delegate = wrapWithRoot(createResolver());

        try {
            this.delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new FilePathPrefixerException(e);
        }
    }

    private FilePathPrefixerAvoidResolve createResolver() {
        PathRegEx resolver = new PathRegEx();
        resolver.setOutPathPrefix(prefix + experimentType);
        resolver.setRegEx(regEx);
        return resolver;
    }

    private Rooted wrapWithRoot(FilePathPrefixerAvoidResolve in) {
        Rooted out = new Rooted();
        out.setRootName(rootName);
        out.setFilePathPrefixer(in);
        return out;
    }

    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }

    public RegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(RegEx regEx) {
        this.regEx = regEx;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
