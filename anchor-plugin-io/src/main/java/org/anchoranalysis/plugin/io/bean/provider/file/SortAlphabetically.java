/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.Collection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.FileProviderException;

public class SortAlphabetically extends FileProvider {

    // START BEAN PROPERTIES
    @BeanField private FileProvider fileProvider;
    // END BEAN PROPERTIES

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {
        return SortUtilities.sortFiles(fileProvider.create(params));
    }

    public FileProvider getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProvider fileProvider) {
        this.fileProvider = fileProvider;
    }
}
