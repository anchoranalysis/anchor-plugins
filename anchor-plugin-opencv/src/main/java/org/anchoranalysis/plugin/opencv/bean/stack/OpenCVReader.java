package org.anchoranalysis.plugin.opencv.bean.stack;

import java.nio.file.Path;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedRaster;
import org.anchoranalysis.plugin.opencv.CVInit;

/**
 * Reads a stack from the file-system using OpenCV.
 * 
 * @author Owen Feehan
 *
 */
public class OpenCVReader extends StackReader {
    
    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }
    
    @Override
    public OpenedRaster openFile(Path path) throws ImageIOException {
        CVInit.blockUntilLoaded();
        return new OpenedRasterOpenCV(path);
    }
}
