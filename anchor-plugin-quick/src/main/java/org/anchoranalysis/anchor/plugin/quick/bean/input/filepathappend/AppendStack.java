/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

/**
 * A stack from a stack-collection
 *
 * @author Owen Feehan
 */
public class AppendStack extends FilePathBaseAppendToManagerWithFileID {

    // START BEAN PROPERTIES
    @BeanField private String extension = "tif";
    // END BEAN PROPERTIES

    @Override
    protected List<NamedBean<FilePathGenerator>> getListFromManager(MultiInputManager inputManager)
            throws BeanMisconfiguredException {
        return inputManager.getAppendStack();
    }

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {
        return String.format(
                "%s/stackCollection/%s.%s", firstPartWithFilename(), getFileId(), extension);
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
