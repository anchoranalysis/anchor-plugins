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

public class SortAlphabeticallyWithDirectory extends FileProviderWithDirectory {

    // START BEAN PROPERTIES
    @BeanField private FileProviderWithDirectory fileProvider;
    // END BEAN PROPERTIES

    @Override
    public Collection<File> matchingFilesForDirectory(Path directory, InputManagerParams params)
            throws FileProviderException {
        return SortUtilities.sortFiles(fileProvider.matchingFilesForDirectory(directory, params));
    }

    public FileProviderWithDirectory getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProviderWithDirectory fileProvider) {
        this.fileProvider = fileProvider;
    }

    @Override
    public Path getDirectoryAsPath(InputContextParams inputContext) {
        return fileProvider.getDirectoryAsPath(inputContext);
    }
}
