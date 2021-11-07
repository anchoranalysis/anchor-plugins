package org.anchoranalysis.plugin.io.bean.metadata.reader;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Infers various image properties from tags in {@link Metadata}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InferAttributesFromMetadata {

    /**
     * Infers the <b>number of channels</i> from the metadata.
     *
     * @param metadata the metadata.
     * @return the number of channels.
     */
    public static Optional<Integer> inferNumberChannels(Metadata metadata) {
        return readInt(metadata, JpegDirectory.class, JpegDirectory.TAG_NUMBER_OF_COMPONENTS);
    }

    /**
     * Infers the <b>bit depth</i> from the metadata.
     *
     * @param metadata the metadata.
     * @return the bit depth.
     */
    public static Optional<Integer> inferBitDepth(Metadata metadata) {
        return readInt(metadata, JpegDirectory.class, JpegDirectory.TAG_DATA_PRECISION);
    }

    private static <T extends Directory> Optional<Integer> readInt(
            Metadata metadata, Class<T> directoryType, int tag) {

        Directory directory = metadata.getFirstDirectoryOfType(directoryType);

        // Search for a width and height directly in the EXIF
        // It is assumed this
        if (directory != null && directory.containsTag(tag)) {
            return Optional.of(directory.getInteger(tag));
        } else {
            return Optional.empty();
        }
    }
}
