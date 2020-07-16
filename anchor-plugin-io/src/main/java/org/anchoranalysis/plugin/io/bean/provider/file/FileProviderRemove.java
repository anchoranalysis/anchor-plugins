/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;

// Removes one or more files if they match a regex
public class FileProviderRemove extends FileProvider {

    // START BEAN PROPERTIES
    @BeanField private FileProvider fileProvider;

    @BeanField private RegEx regEx; // Paths to remove
    // END BEAN PROPERTIES

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {

        Collection<File> files = fileProvider.create(params);

        // Loop through each file and see if it's in our has map
        Iterator<File> itr = files.iterator();
        while (itr.hasNext()) {
            File f = itr.next();

            String normalizedPath = FilePathToUnixStyleConverter.toStringUnixStyle(f.toPath());
            if (regEx.hasMatch(normalizedPath)) {
                itr.remove();
            }
        }

        return files;
    }

    public RegEx getRegEx() {
        return regEx;
    }

    public void setRegEx(RegEx regEx) {
        this.regEx = regEx;
    }

    public FileProvider getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProvider fileProvider) {
        this.fileProvider = fileProvider;
    }
}
