/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.fileprovider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.bean.input.AnnotationInputManager;
import org.anchoranalysis.annotation.io.bean.strategy.AnnotatorStrategy;
import org.anchoranalysis.annotation.io.input.AnnotationWithStrategy;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.io.input.NamedChnlsInputPart;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;

public class FileProviderFromAnnotation<T extends AnnotatorStrategy> extends FileProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private AnnotationInputManager<NamedChnlsInputPart, T> annotationInputManager;
    // END BEAN PROPERTIES

    @Override
    public Collection<File> create(InputManagerParams params) throws FileProviderException {

        List<File> filesOut = new ArrayList<>();

        try {
            List<AnnotationWithStrategy<T>> list = annotationInputManager.inputObjects(params);
            for (AnnotationWithStrategy<T> inp : list) {

                filesOut.addAll(inp.deriveAssociatedFiles());
            }

        } catch (AnchorIOException e) {
            throw new FileProviderException(e);
        }

        return filesOut;
    }
}
