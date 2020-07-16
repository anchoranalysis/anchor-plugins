/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.fileprovider;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.annotation.io.mark.MarkAnnotationReader;
import org.anchoranalysis.annotation.mark.MarkAnnotation;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.generator.FilePathGenerator;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;

public class FilterForAcceptedAnnotations extends FileProvider {

    // START BEAN PROPERTIES
    @BeanField private FileProvider fileProvider;

    @BeanField private List<FilePathGenerator> listFilePathGenerator = new ArrayList<>();
    // END BEAN PROPERTIES

    private MarkAnnotationReader annotationReader = new MarkAnnotationReader(false);

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {
        try {
            Collection<File> filesIn = fileProvider.create(params);

            List<File> filesOut = new ArrayList<>();

            for (File f : filesIn) {

                if (isFileAccepted(f)) {
                    filesOut.add(f);
                }
            }

            return filesOut;
        } catch (AnchorIOException e) {
            throw new FileProviderException(e);
        }
    }

    public List<FilePathGenerator> getListFilePathGenerator() {
        return listFilePathGenerator;
    }

    public void setListFilePathGenerator(List<FilePathGenerator> listFilePathGenerator) {
        this.listFilePathGenerator = listFilePathGenerator;
    }

    public FileProvider getFileProvider() {
        return fileProvider;
    }

    public void setFileProvider(FileProvider fileProvider) {
        this.fileProvider = fileProvider;
    }

    private boolean isFileAccepted(File file) throws AnchorIOException {

        for (FilePathGenerator fpg : listFilePathGenerator) {
            Path annotationPath = fpg.outFilePath(file.toPath(), false);

            Optional<MarkAnnotation> annotation = annotationReader.read(annotationPath);

            if (!annotation.isPresent() || !annotation.get().isAccepted()) {
                return false;
            }
        }
        return true;
    }
}
