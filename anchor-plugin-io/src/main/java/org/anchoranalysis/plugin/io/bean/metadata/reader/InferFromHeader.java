package org.anchoranalysis.plugin.io.bean.metadata.reader;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.metadata.reader.ImageMetadataReader;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.plugin.io.bean.metadata.header.JPEG;
import org.anchoranalysis.plugin.io.bean.metadata.header.HeaderFormat;

/**
 * Tries to construct the {@link ImageMetadata} from EXIF and other metadata, if available, or
 * otherwise falls back to another reader.
 *
 * <p>It supports a limited number of file-types, as identified by an extension in the path. By default, it supports:
 *
 * <ul>
 *   <li>JPEG (.jpg or .jpeg)
 *   <li>PNG (.png)
 * </ul>
 *
 * @author Owen Feehan
 */
@NoArgsConstructor @AllArgsConstructor
public class InferFromHeader extends ImageMetadataReader {
    
    // START BEAN PROPERTIES
    /** Fallback to use if EXIF information is non-existing or absent. */
    @BeanField @Getter @Setter private ImageMetadataReader fallback;
    
    /** The formats whose headers will be searched, to find sufficient metadata to populate {@link ImageMetadata}. */
    @BeanField @Getter @Setter private List<HeaderFormat> formats = createDefaultFormats();
    // END BEAN PROPERTIES

    @Override
    public ImageMetadata openFile(Path path, StackReader defaultStackReader)
            throws ImageIOException {

       return OptionalUtilities.orElseGet( attemptToPopulateFromMetadata(path),
            () -> useFallbackReader(path, defaultStackReader));
    }

    /**
     * Try to infer the needed elements for {@link ImageMetadata} from metadata present in {@code
     * path}.
     */
    private Optional<ImageMetadata> attemptToPopulateFromMetadata(Path path)
            throws ImageIOException {
        for( HeaderFormat format : formats) {
            Optional<ImageMetadata> metadata = format.populateFrom(path);
            if (metadata.isPresent()) {
                return Optional.of(metadata.get());
            }
        }
        return Optional.empty();
    }
    
    /** Use the fallback {@code ImageMetadataReader} to establish the metadata. */
    private ImageMetadata useFallbackReader(Path path, StackReader defaultStackReader)
            throws ImageIOException {
        return fallback.openFile(path, defaultStackReader);
    }
    
    private static List<HeaderFormat> createDefaultFormats() {
        return Arrays.asList( new JPEG() );
    }
}