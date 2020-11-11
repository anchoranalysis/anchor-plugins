package org.anchoranalysis.plugin.io.bean.input.files;

import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithDirectory;
import org.anchoranalysis.io.input.files.FileWithDirectoryInput;
import lombok.Getter;
import lombok.Setter;

public class NamedFilesWithDirectory extends NamedFilesBase<FileWithDirectoryInput> {

    // START BEAN PROPERTIES
    /** The files to use as inputs. */
    @BeanField @Getter @Setter private FilesProviderWithDirectory filesProvider;
    // END BEAN PROPERTIES
    
    @Override
    public List<FileWithDirectoryInput> inputs(InputManagerParams params)
            throws InputReadFailedException {
        return createInputsFromFiles(filesProvider, params, namedFile -> new FileWithDirectoryInput(namedFile, filesProvider.getDirectoryAsPath(params.getInputContext())) );
    }

}
