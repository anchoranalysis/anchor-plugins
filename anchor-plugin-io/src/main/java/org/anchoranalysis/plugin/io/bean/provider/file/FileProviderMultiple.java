/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporterMultiple;
import org.anchoranalysis.core.progress.ProgressReporterOneOfMany;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.FileProviderException;

public class FileProviderMultiple extends FileProvider {

    // START BEAN PROPERTIES
    @BeanField private List<FileProvider> list = new ArrayList<>();
    // END BEAN PROPERTIES

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {

        try (ProgressReporterMultiple prm =
                new ProgressReporterMultiple(params.getProgressReporter(), list.size())) {

            List<File> combined = new ArrayList<>();

            for (FileProvider fp : list) {

                ProgressReporterOneOfMany prLocal = new ProgressReporterOneOfMany(prm);
                combined.addAll(fp.create(params.withProgressReporter(prLocal)));
                prm.incrWorker();
            }
            return combined;
        }
    }

    public List<FileProvider> getList() {
        return list;
    }

    public void setList(List<FileProvider> list) {
        this.list = list;
    }
}
