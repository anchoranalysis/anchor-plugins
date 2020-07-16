/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;
import org.anchoranalysis.mpp.sgmn.define.OutputterDirectories;

public class AppendObjects extends FilePathBaseAppendToManagerWithFileID {

    @Override
    protected List<NamedBean<FilePathGenerator>> getListFromManager(MultiInputManager inputManager)
            throws BeanMisconfiguredException {
        return inputManager.getAppendObjects();
    }

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {
        return String.format(
                "%s/%s/%s/", firstPartWithFilename(), OutputterDirectories.OBJECT, getFileId());
    }
}
