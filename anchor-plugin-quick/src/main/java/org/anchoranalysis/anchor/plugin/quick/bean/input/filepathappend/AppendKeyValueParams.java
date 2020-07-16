/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.mpp.io.bean.input.MultiInputManager;

public class AppendKeyValueParams extends FilePathBaseAppendToManager {

    // START BEAN PROPERTIES
    @BeanField private boolean includeFileName = false;

    // If non-empty this directory is appended to make the out path string
    @BeanField @AllowEmpty private String subDirectory = "";
    // END BEAN PROPERTIES

    @Override
    protected List<NamedBean<FilePathGenerator>> getListFromManager(MultiInputManager inputManager)
            throws BeanMisconfiguredException {
        return inputManager.getListAppendKeyValueParams();
    }

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {

        String firstPart = selectMaybeDir(selectFirstPart());

        return String.format("%s/keyValueParams.xml", firstPart);
    }

    private String selectMaybeDir(String firstPart) {

        if (!subDirectory.isEmpty()) {
            return String.format("%s/%s", firstPart, subDirectory);
        } else {
            return firstPart;
        }
    }

    private String selectFirstPart() {
        if (includeFileName) {
            return firstPartWithFilename();
        } else {
            return firstPart();
        }
    }

    public boolean isIncludeFileName() {
        return includeFileName;
    }

    public void setIncludeFileName(boolean includeFileName) {
        this.includeFileName = includeFileName;
    }

    public String getSubDirectory() {
        return subDirectory;
    }

    public void setSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory;
    }
}
