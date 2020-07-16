/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;

/** A CSV that is appended from the root of a project */
public class AppendCsv extends FilePathAppendBase {

    // START BEAN PROPERTIES
    /**
     * Unique identifier for the stack, object-mask-collection etc. that is being appended. Filename
     * is constructed from it
     */
    @BeanField private String fileId;
    // END BEAN PROPERTIES

    @Override
    protected String createOutPathString() throws BeanMisconfiguredException {
        return String.format("%s/%s.csv", firstPart(), getFileId());
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
