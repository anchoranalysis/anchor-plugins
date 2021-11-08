package org.anchoranalysis.plugin.io.bean.metadata.header;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;

/**
 * Populates {@link ImageMetadata} from the header of an image-file.
 *
 * <p>The <a href="https://github.com/drewnoakes/metadata-extractor">metadata-extractor</a> from
 * Drew Noakes is used to read the metadata.
 *
 * @author Owen Feehan
 */
public abstract class HeaderFormat extends AnchorBean<HeaderFormat> {

    /**
     * Creates a {@link ImageMetadata}, if possible, from the metadata at {@code path}.
     *
     * @param path the path to an image file.
     * @return the metadata associated with {@code path}, if it was possible to infer it.
     * @throws ImageIOException if the metadata does not meet expectations or I/O fails.
     */
    public Optional<ImageMetadata> populateFrom(Path path) throws ImageIOException {
        try {
            if (format().matches(path)) {
                Metadata metadata =
                        com.drew.imaging.ImageMetadataReader.readMetadata(path.toFile());

                if (metadata == null) {
                    return Optional.empty();
                }

                return populateFromMetadata(metadata);
            } else {
                return Optional.empty();
            }

        } catch (IOException | ImageProcessingException e) {
            throw new ImageIOException(e);
        }
    }

    /**
     * The associated {@link ImageFileFormat} with this header.
     *
     * <p>Only files whose paths end with an extension for this format will be accepted.
     *
     * @return the format.
     */
    protected abstract ImageFileFormat format();

    /**
     * Creates a {@link ImageMetadata}, if possible, from {@code metadata}.
     *
     * @param metadata the {@link Metadata} to infer {@link ImageMetadata} from.
     * @return the inferred metadata, if it was possible to infer it.
     * @throws ImageIOException if the metadata does not meet expectations.
     */
    protected abstract Optional<ImageMetadata> populateFromMetadata(Metadata metadata)
            throws ImageIOException;
}
