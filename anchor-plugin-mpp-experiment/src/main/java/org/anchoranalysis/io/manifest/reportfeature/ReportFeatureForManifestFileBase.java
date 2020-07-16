/* (C)2020 */
package org.anchoranalysis.io.manifest.reportfeature;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;

public abstract class ReportFeatureForManifestFileBase extends ReportFeatureForManifest {

    // START BEAN PROPERTIES
    @BeanField private String fileName = "";

    @BeanField private String title = "";
    // END BEAN PROPERTIES

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public String genTitleStr() throws OperationFailedException {
        return title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
