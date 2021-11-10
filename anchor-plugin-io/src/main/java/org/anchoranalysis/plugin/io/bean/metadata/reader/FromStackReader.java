package org.anchoranalysis.plugin.io.bean.metadata.reader;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;

/**
 * Uses a {@link StackReader} to read the image-metadata.
 *
 * @author Owen Feehan
 */
public class FromStackReader extends ImageMetadataReader {

    // START BEAN PROPERTIES
    /**
     * The {@link StackReader} to use to read the image metadata.
     *
     * <p>If not specified, the default {@link StackReader} is used. Note that this bean does not
     * allow the default -instance to be passed by {@link DefaultInstance}, as the bean often needs
     * to be specified in the {@code defaultBeans.xml} configuration file, where it is not already
     * known.
     */
    @BeanField @Getter @Setter @OptionalBean private StackReader stackReader;

    /** The series to open to the read the metadata (zero-indexed). */
    @BeanField @Getter @Setter private int seriesIndex = 0;
    // END BEAN PROPERTIES

    @Override
    public ImageMetadata openFile(Path path, StackReader defaultStackReader, Logger logger)
            throws ImageIOException {

        StackReader selectedReader = Optional.ofNullable(stackReader).orElse(defaultStackReader);

        try (OpenedImageFile file = selectedReader.openFile(path, logger)) {
            return file.metadata(seriesIndex);
        }
    }
}
