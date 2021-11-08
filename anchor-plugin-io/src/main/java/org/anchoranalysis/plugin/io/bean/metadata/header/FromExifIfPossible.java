package org.anchoranalysis.plugin.io.bean.metadata.header;

import com.drew.metadata.Metadata;
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
 * <p>It tries first to read from any EXIF header, and if that is absent, then a fallback.
 *
 * <p>All are presumes to describe the image <b><i>before</i></b> any {@link OrientationChange} is
 * applied.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class FromExifIfPossible {

    /**
     * Infers the {@link Extent} from the metadata.
     *
     * @param metadata the metadata.
     * @param orientation manipulation to apply to any width/height in {@code metadata} after it is
     *     read.
     * @return the extent, if it is existed, orientated to match {@code orientation}.
     * @throws ImageIOException if metadata exists in an invalid state.
     */
    public static Optional<Extent> inferExtentFromEXIFOr(
            Metadata metadata, Optional<OrientationChange> orientation) throws ImageIOException {

        Optional<Extent> extent =
                OptionalUtilities.orFlatSupplier(
                        () -> readExif(metadata), () -> readOther(metadata));

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
        return InferHelper.readFromWidthHeightTags(
                metadata,
                ExifIFD0Directory.class,
                ExifIFD0Directory.TAG_IMAGE_WIDTH,
                ExifIFD0Directory.TAG_IMAGE_HEIGHT);
    }

    private static Optional<Extent> readOther(Metadata metadata) throws ImageIOException {
        return InferHelper.readFromWidthHeightTags(
                metadata,
                JpegDirectory.class,
                JpegDirectory.TAG_IMAGE_WIDTH,
                JpegDirectory.TAG_IMAGE_HEIGHT);
    }
}
