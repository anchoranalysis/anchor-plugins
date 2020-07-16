/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;

public abstract class CopyFilesMethod {

    public void createDestinationFile(Path source, Path destination) throws CreateException {

        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            throw new CreateException(e);
        }

        createWithDir(source, destination);
    }

    protected abstract void createWithDir(Path source, Path destination) throws CreateException;
}
