/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.copymethod;

import java.nio.file.Path;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;

public class CompressTIFF extends CopyFilesMethod {

    private SimpleCopy simpleCopy = new SimpleCopy();

    @Override
    public void createWithDir(Path source, Path destination) throws CreateException {

        String fileName = source.getFileName().toString().toLowerCase();

        try {
            if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")) {
                CopyTIFFAndCompress.apply(source.toString(), destination);
            } else {
                simpleCopy.createDestinationFile(source, destination);
            }
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
