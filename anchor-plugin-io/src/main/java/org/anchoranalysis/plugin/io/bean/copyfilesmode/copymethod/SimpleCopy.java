/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.anchoranalysis.core.error.CreateException;

public class SimpleCopy extends CopyFilesMethod {

    @Override
    public void createWithDir(Path source, Path destination) throws CreateException {
        try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CreateException(e);
        }
    }
}
