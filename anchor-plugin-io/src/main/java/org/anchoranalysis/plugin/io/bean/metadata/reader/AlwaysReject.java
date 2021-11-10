package org.anchoranalysis.plugin.io.bean.metadata.reader;

import java.nio.file.Path;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;

/**
 * A special {@link ImageMetadataReader} that will always throw an exception and reject a file.
 *
 * <p>This can be useful for debugging errors, to identify circumstances in which a {@link
 * ImageMetadataReader} is used or not used.
 *
 * @author Owen Feehan
 */
public class AlwaysReject extends ImageMetadataReader {

    @Override
    public ImageMetadata openFile(Path path, StackReader defaultStackReader, ErrorReporter errorReporter)
            throws ImageIOException {
        throw new ImageIOException(
                "This image has been rejected by the ImageMetadataReader by design.");
    }
}
