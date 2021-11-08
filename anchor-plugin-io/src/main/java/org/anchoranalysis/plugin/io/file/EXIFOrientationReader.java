package org.anchoranalysis.plugin.io.file;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.core.dimensions.OrientationChange;
import org.anchoranalysis.image.io.ImageIOException;

/**
 * Reads the EXIF orientation, if specified, from an image-file.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EXIFOrientationReader {

    /**
     * Determines the needed correction to orientation for the voxels if an EXIF orientation tag is
     * present.
     *
     * @param path the path to the image, which possibly has EXIF metadata.
     * @return the needed correction, if it can be determined, or {@link Optional#empty} if no EXIF
     *     orientation tag is present.
     * @throws ImageIOException if the EXIF orientation tag is present, but unsupported.
     */
    public static Optional<OrientationChange> determineOrientationCorrection(Path path)
            throws ImageIOException {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(path.toFile());

            if (metadata == null) {
                return Optional.empty();
            }

            return determineOrientationCorrection(metadata);

        } catch (IOException | ImageProcessingException e) {
            throw new ImageIOException(e);
        }
    }

    /**
     * Determines the needed correction to orientation for the voxels if an EXIF orientation tag is
     * present.
     *
     * @param metadata the metadata for the image.
     * @return the needed correction, if it can be determined, or {@link Optional#empty} if no EXIF
     *     orientation tag is present.
     * @throws ImageIOException if the EXIF orientation tag is present, but unsupported.
     */
    public static Optional<OrientationChange> determineOrientationCorrection(Metadata metadata)
            throws ImageIOException {
        try {
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory == null) {
                return Optional.empty();
            }
            if (directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                return Optional.of(decodeOrientationTag(orientation));
            } else {
                return Optional.empty();
            }

        } catch (MetadataException e) {
            throw new ImageIOException(e);
        }
    }

    /**
     * Infers the necessary clockwise rotation to auto-correct the orientation.
     *
     * <p>The <a href="https://jdhao.github.io/2019/07/31/image_rotation_exif_info/">blog</a>
     * provides a reference for the various EXIF orientation flag values.
     *
     * @param orientation the EXIF tag describing the orientation.
     * @return the angle in degrees of the clockwise rotation necessary to correct the orientation
     *     to left-upwards.
     * @throws ImageIOException if the specified orientation flag is unsupported.
     */
    private static OrientationChange decodeOrientationTag(int orientation) throws ImageIOException {
        switch (orientation) {
            case 0:
            case 1:
                // Already matching our voxels, so no need to rotate further.
                return OrientationChange.KEEP_UNCHANGED;

            case 2:
                return OrientationChange.MIRROR_WITHOUT_ROTATION;

            case 3:
                return OrientationChange.ROTATE_180;

            case 4:
                return OrientationChange.ROTATE_180_MIRROR;

            case 5:
                return OrientationChange.ROTATE_90_ANTICLOCKWISE_MIRROR;

            case 6:
                return OrientationChange.ROTATE_90_ANTICLOCKWISE;

            case 7:
                return OrientationChange.ROTATE_90_CLOCKWISE_MIRROR;

            case 8:
                return OrientationChange.ROTATE_90_CLOCKWISE;

            default:
                throw unsupportedOrientationFlagException(orientation);
        }
    }

    private static ImageIOException unsupportedOrientationFlagException(int orientationFlag) {
        return new ImageIOException(
                String.format("EXIF Orientation Flag %d is unsupported.", orientationFlag));
    }
}