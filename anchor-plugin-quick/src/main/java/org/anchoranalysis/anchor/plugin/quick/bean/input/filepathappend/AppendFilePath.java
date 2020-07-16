/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

public class AppendFilePath extends FilePathBaseAppendToManagerWithFileID {

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {
        return String.format("%s/%s", firstPart(), getFileId());
    }

    @Override
    protected List<NamedBean<FilePathGenerator>> getListFromManager(MultiInputManager inputManager)
            throws BeanMisconfiguredException {
        return inputManager.getListAppendFilePath();
    }
}
