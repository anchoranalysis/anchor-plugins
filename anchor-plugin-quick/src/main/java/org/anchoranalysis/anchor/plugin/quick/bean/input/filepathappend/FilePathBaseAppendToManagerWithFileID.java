/* (C)2020 */
package org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend;

import org.anchoranalysis.bean.annotation.BeanField;

public abstract class FilePathBaseAppendToManagerWithFileID extends FilePathBaseAppendToManager {

    // START BEAN PROPERTIES
    /**
     * Unique identifier for the stack, object-mask-collection etc. that is being appended. Filename
     * is constructed from it
     */
    @BeanField private String fileId;
    // END BEAN PROPERTIES

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
