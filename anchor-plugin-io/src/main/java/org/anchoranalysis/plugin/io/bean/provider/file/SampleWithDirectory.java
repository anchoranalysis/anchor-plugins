/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.params.InputContextParams;

public class SampleWithDirectory extends FileProviderWithDirectory {

    // START BEAN PROPERTIES
    @BeanField private FileProviderWithDirectory fileProvider;

    @BeanField private int sampleEvery;
    // END BEAN PROPERTIES

    @Override
    public Path getDirectoryAsPath(InputContextParams inputContext) {
        return fileProvider.getDirectoryAsPath(inputContext);
    }

    @Override
    public Collection<File> matchingFilesForDirectory(Path directory, InputManagerParams params)
            throws FileProviderException {

        List<File> listSampled = new ArrayList<>();

        Collection<File> list = fileProvider.create(params);

        int cnt = -1; // So the first item becomes 0
        for (File f : list) {
            cnt++;
            if (cnt == sampleEvery) {
                listSampled.add(f);
                cnt = 0;
            }
        }

        return listSampled;
    }

    public int getSampleEvery() {
        return sampleEvery;
    }

    public void setSampleEvery(int sampleEvery) {
        this.sampleEvery = sampleEvery;
    }

    public FileProviderWithDirectory getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProviderWithDirectory fileProvider) {
        this.fileProvider = fileProvider;
    }
}
