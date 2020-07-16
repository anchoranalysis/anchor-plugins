/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

public abstract class FilePathBaseAppendToManager extends FilePathAppendBase {

    /**
     * @param inputManager
     * @param rootName if non-empty (and non-NULL) a rooted filePathGenerator is created instead of
     *     a non rooted
     * @param regex a regular-expression that returns two groups, the first is the dataset name, the
     *     second is the file-name
     * @throws BeanMisconfiguredException
     */
    public void addToManager(MultiInputManager inputManager, String rootName, String regex)
            throws BeanMisconfiguredException {
        NamedBean<FilePathGenerator> nb = createFilePathGenerator(rootName, regex);
        getListFromManager(inputManager).add(nb);
    }

    protected abstract List<NamedBean<FilePathGenerator>> getListFromManager(
            MultiInputManager inputManager) throws BeanMisconfiguredException;
}
