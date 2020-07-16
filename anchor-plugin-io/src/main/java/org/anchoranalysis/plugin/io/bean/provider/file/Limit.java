/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.Collection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.FileProviderException;

public class Limit extends FileProvider {

    // START BEANS
    @BeanField private FileProvider fileProvider;

    @BeanField private int maxNumItems = 0;
    // END BEANS

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {
        return LimitUtilities.apply(fileProvider.create(params), maxNumItems);
    }

    public FileProvider getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProvider fileProvider) {
        this.fileProvider = fileProvider;
    }

    public int getMaxNumItems() {
        return maxNumItems;
    }

    public void setMaxNumItems(int maxNumItems) {
        this.maxNumItems = maxNumItems;
    }
}
