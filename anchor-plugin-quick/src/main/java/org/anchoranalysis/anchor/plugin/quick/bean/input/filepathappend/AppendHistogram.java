/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

public class AppendHistogram extends FilePathBaseAppendToManagerWithFileID {

    // START BEAN PROPERTIES
    @BeanField private String group = "sum";
    // END BEAN PROPERTIES

    @Override
    protected List<NamedBean<FilePathGenerator>> getListFromManager(MultiInputManager inputManager)
            throws BeanMisconfiguredException {
        return inputManager.getAppendHistogram();
    }

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {
        return String.format(
                "%s/%s", firstPartWithCustomMiddle(String.format("/%s/", group)), getFileId());
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
