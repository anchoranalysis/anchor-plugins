/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.params.InputContextParams;

public class LimitWithDirectory extends FileProviderWithDirectory {

    // START BEANS
    @BeanField private FileProviderWithDirectory fileProvider;

    @BeanField private int maxNumItems = 0;
    // END BEANS

    @Override
    public Collection<File> matchingFilesForDirectory(Path directory, InputManagerParams params)
            throws FileProviderException {
        return LimitUtilities.apply(
                fileProvider.matchingFilesForDirectory(directory, params), maxNumItems);
    }

    @Override
    public Path getDirectoryAsPath(InputContextParams inputContext) {
        return fileProvider.getDirectoryAsPath(inputContext);
    }

    public FileProviderWithDirectory getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProviderWithDirectory fileProvider) {
        this.fileProvider = fileProvider;
    }

    public int getMaxNumItems() {
        return maxNumItems;
    }

    public void setMaxNumItems(int maxNumItems) {
        this.maxNumItems = maxNumItems;
    }
}
