package org.anchoranalysis.plugin.io.bean.metadata.header;

import java.util.Optional;
import org.anchoranalysis.core.format.ImageFileFormat;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.io.bioformats.metadata.ReadMetadataUtilities;
import org.anchoranalysis.spatial.box.Extent;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.bmp.BmpHeaderDirectory;


/**
 * The headers found in a BMP file.
 *
 * @author Owen Feehan
 */
public class BMP extends HeaderFormat {

    @Override
    protected ImageFileFormat format() {
        return ImageFileFormat.BMP;
    }

    @Override
    protected Optional<ImageMetadata> populateFromMetadata(
            Metadata metadata, ImageFileAttributes timestamps) throws ImageIOException {

        Directory directory = metadata.getFirstDirectoryOfType(BmpHeaderDirectory.class);
        if (directory==null) {
            return Optional.empty();
        }

        Optional<Extent> extent =
                ReadMetadataUtilities.readFromWidthHeightTags(
                        directory,
                        BmpHeaderDirectory.TAG_IMAGE_WIDTH,
                        BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
        if (!extent.isPresent()) {
            return Optional.empty();
        }

        /*Optional<Integer> numberChannels = numberOfChannels(directory.get());
        if (!numberChannels.isPresent()) {
            return Optional.empty();
        }*/

        Optional<Integer> bitDepth =
                ReadMetadataUtilities.readInt(directory, BmpHeaderDirectory.TAG_BITS_PER_PIXEL);
        if (!bitDepth.isPresent()) {
            return Optional.empty();
        }
        
        switch (bitDepth.get()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                return createMetadata(extent.get(), 1, 8, timestamps);
            case 16:
                return createMetadata(extent.get(), 2, 8, timestamps);
            case 24:
                return createMetadata(extent.get(), 3, 8, timestamps);                
            case 32:
                return createMetadata(extent.get(), 4, 8, timestamps);                
            default:
                throw new ImageIOException( String.format("Unrecognised bitsPerPixel of %d", bitDepth.get()));
        }
    }
    
    private static Optional<ImageMetadata> createMetadata(Extent extent, int numberChannels, int pixelDepth, ImageFileAttributes timestamps) {
        return Optional.of(MetadataFactory.createMetadata(extent, numberChannels, pixelDepth, timestamps));
    }

}
