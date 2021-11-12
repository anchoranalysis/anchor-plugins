package org.anchoranalysis.plugin.io.bean.metadata.header;

import java.util.Optional;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.spatial.box.Extent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class MetadataFactory {

    /** Creates the {@link ImageMetadata} given the necessary ingredients. */
    public static ImageMetadata createMetadata(
            Extent extent, int numberChannels, int bitDepth, ImageFileAttributes timestamps) {
        // Image resolution is ignored.
        Dimensions dimensions = new Dimensions(extent);
        boolean rgb = numberChannels == 3 || numberChannels == 4;
        return new ImageMetadata(
                dimensions, numberChannels, 1, rgb, bitDepth, timestamps, Optional.empty());
    }
}
