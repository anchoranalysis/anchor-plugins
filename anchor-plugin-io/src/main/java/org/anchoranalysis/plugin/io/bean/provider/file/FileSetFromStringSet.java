/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectoryString;

public class FileSetFromStringSet extends FileProviderWithDirectoryString {

    // START BEAN PROPERTIES
    @BeanField private StringSet filePaths;
    // END BEAN PROPERTIES

    public Collection<File> matchingFilesForDirectory(Path directory, InputManagerParams params) {

        List<File> files = new ArrayList<>();

        for (String s : filePaths) {
            Path relPath = Paths.get(s);
            files.add(directory.resolve(relPath).toFile());
        }

        return files;
    }

    public StringSet getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(StringSet filePaths) {
        this.filePaths = filePaths;
    }
}
