package org.anchoranalysis.plugin.io.bean.metadata.reader;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Infers the size of the image from tags in {@link Metadata}.
 *
 * <p>It tries, in this order:
 *
 * <ul>
 *   <li>any EXIF header
 *   <li>any JPEG header
 * </ul>
 *
 * All are presumes to describe the image <b><i>before</i></b> any {@link OrientationChange} is
 * applied.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InferExtentFromMetadata {

    /**
     * Infers the {@link Extent} from the metadata.
     *
     * @param metadata the metadata.
     * @param orientation manipulation to apply to any width/height in {@code metadata} after it is
     *     read.
     * @return the extent, if it is existed, orientated to match {@code orientation}.
     * @throws ImageIOException if metadata exists in an invalid state.
     */
    public static Optional<Extent> inferExtent(
            Metadata metadata, Optional<OrientationChange> orientation) throws ImageIOException {

        Optional<Extent> extent =
                OptionalUtilities.orFlatSupplier(
                        () -> readExif(metadata), () -> readJPEG(metadata));

        if (extent.isPresent()) {
            if (orientation.isPresent()) {
                return Optional.of(orientation.get().extent(extent.get()));
            } else {
                return extent;
            }
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Extent> readExif(Metadata metadata) throws ImageIOException {
        return readFromWidthHeightTags(
                metadata,
                ExifIFD0Directory.class,
                ExifIFD0Directory.TAG_IMAGE_WIDTH,
                ExifIFD0Directory.TAG_IMAGE_HEIGHT);
    }

    private static Optional<Extent> readJPEG(Metadata metadata) throws ImageIOException {
        return readFromWidthHeightTags(
                metadata,
                JpegDirectory.class,
                JpegDirectory.TAG_IMAGE_WIDTH,
                JpegDirectory.TAG_IMAGE_HEIGHT);
    }

    private static <T extends Directory> Optional<Extent> readFromWidthHeightTags(
            Metadata metadata, Class<T> directoryType, int tagWidth, int tagHeight)
            throws ImageIOException {

        Directory directory = metadata.getFirstDirectoryOfType(directoryType);

        // Search for a width and height directly in the EXIF
        // It is assumed this
        if (directory != null
                && directory.containsTag(tagWidth)
                && directory.containsTag(tagHeight)) {

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
