/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.input.file;

import java.io.File;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FileProviderException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.descriptivename.RemoveExtensions;
import org.anchoranalysis.plugin.io.bean.descriptivename.patternspan.PatternSpan;

/**
 * File-paths
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class Files extends InputManager<FileInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FileProvider fileProvider;

    @BeanField @Getter @Setter
    private DescriptiveNameFromFile descriptiveNameFromFile =
            new RemoveExtensions(new PatternSpan());
    // END BEAN PROPERTIES

    public Files(FileProvider fileProvider) {
        this.fileProvider = fileProvider;
    }

    @Override
    public List<FileInput> inputObjects(InputManagerParams params) throws AnchorIOException {
        try {
            Collection<File> files = getFileProvider().create(params);

            return FunctionalList.mapToList(
                    descriptiveNameFromFile.descriptiveNamesForCheckUniqueness(
                            files, params.getLogger()),
                    FileInput::new);
        } catch (FileProviderException e) {
            throw new AnchorIOException("Cannot find files", e);
        }
    }
}
