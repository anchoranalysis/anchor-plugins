package org.anchoranalysis.plugin.io.bean.metadata.header;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.png.PngDirectory;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Infers various image properties from tags in {@link Metadata}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InferHelper {

    /**
     * Finds the first {@link Directory} in the metadata with type {@code directoryType} and whose
     * name is equal to {@code directoryName}.
     *
     * @param <T> directory-type to find
     * @param metadata the metadata to read from.
     * @param directoryType class corresponding to {@code T}.
     * @param directoryName the name of the directory, which must match, case insensitive.
     * @return the first directory that matches, if it exists.
     */
    public static <T extends Directory> Optional<Directory> findDirectoryWithName(
            Metadata metadata, Class<T> directoryType, String directoryName) {
        for (PngDirectory directory : metadata.getDirectoriesOfType(PngDirectory.class)) {
            if (directory.getName().equalsIgnoreCase(directoryName)) {
                return Optional.of(directory);
            }
        }
        return Optional.empty();
    }

    /**
     * Reads a metadata entry of type {@code int} from the first directory of type {@code
     * directoryType}.
     *
     * @param <T> directory-type to find
     * @param metadata the metadata to read from.
     * @param directoryType class corresponding to {@code T}.
     * @param tag a unique identifier from the metadata-extractor library identifying which tag to
     *     read.
     * @return the value of the tag, or {@link Optional#empty()} if it does not exist.
     */
    public static <T extends Directory> Optional<Integer> readInt(
            Metadata metadata, Class<T> directoryType, int tag) {

        Directory directory = metadata.getFirstDirectoryOfType(directoryType);
        return readInt(directory, tag);
    }

    /**
     * Reads a metadata entry of type {@code int} from a {@link Directory}.
     *
     * @param directory the directory to read from.
     * @param tag a unique identifier from the metadata-extractor library identifying which tag to
     *     read.
     * @return the value of the tag, or {@link Optional#empty()} if it does not exist.
     */
    public static Optional<Integer> readInt(Directory directory, int tag) {

        // Search for a width and height directly in the EXIF
        // It is assumed this
        if (directory != null && directory.containsTag(tag)) {
            return Optional.of(directory.getInteger(tag));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Reads two metadata entries, representing width and height, and use them to form a {@link
     * Extent}.
     *
     * <p>The first directory of type {@code directoryType} is used for the tags.
     *
     * @param <T> directory-type to find
     * @param metadata the metadata to read from.
     * @param directoryType class corresponding to {@code T}.
     * @param tagWidth a unique identifier from the metadata-extractor library identifying the
     *     <i>width</i> tag.
     * @param tagHeight a unique identifier from the metadata-extractor library identifying the
     *     <i>height</i> tag.
     * @return the value of the tag, or {@link Optional#empty()} if it does not exist.
     */
    public static <T extends Directory> Optional<Extent> readFromWidthHeightTags(
            Metadata metadata, Class<T> directoryType, int tagWidth, int tagHeight)
            throws ImageIOException {

        Directory directory = metadata.getFirstDirectoryOfType(directoryType);

        // Search for a width and height directly in the EXIF
        // It is assumed this
        if (directory != null) {
            return readFromWidthHeightTags(directory, tagWidth, tagHeight);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Reads two metadata entries, representing width and height, and use them to form a {@link
     * Extent}.
     *
     * @param directory the directory to read tags from.
     * @param tagWidth a unique identifier from the metadata-extractor library identifying the
     *     <i>width</i> tag.
     * @param tagHeight a unique identifier from the metadata-extractor library identifying the
     *     <i>height</i> tag.
     * @return the value of the tag, or {@link Optional#empty()} if it does not exist.
     */
    public static Optional<Extent> readFromWidthHeightTags(
            Directory directory, int tagWidth, int tagHeight) throws ImageIOException {

        if (directory.containsTag(tagWidth) && directory.containsTag(tagHeight)) {

            try {
                int width = directory.getInt(tagWidth);
                int height = directory.getInt(tagHeight);

                if (width == 0 || height == 0) {
                    throw new ImageIOException(
                            "A width or height of 0 was specified in metadata, which suggests a format is not supported e.g perhaps JPEGs with DNL markers.");
                }
                return Optional.of(new Extent(width, height));

            } catch (MetadataException e) {
                throw new ImageIOException("Image metadata exists in an invalid state.", e);
            }

        } else {
            return Optional.empty();
        }
    }
}
