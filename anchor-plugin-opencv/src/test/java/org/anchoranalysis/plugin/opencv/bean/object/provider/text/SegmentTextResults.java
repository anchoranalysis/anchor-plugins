package org.anchoranalysis.plugin.opencv.bean.object.provider.text;

import java.util.Arrays;
import java.util.List;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.box.BoundingBox;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
class SegmentTextResults {

    private static final BoundingBox BOX1_RGB = boxAt(312, 319, 107, 34);
    private static final BoundingBox BOX1_GRAYSCALE = boxAt(316, 319, 104, 33);
    
    private static final BoundingBox BOX2_RGB = boxAt(394, 200, 27, 25);
    private static final BoundingBox BOX2_GRAYSCALE = boxAt(394, 199, 27, 27);
    
    private static final BoundingBox BOX3 = boxAt(440, 312, 73, 36); 
    
    public static List<BoundingBox> rgb() {
        return Arrays.asList(BOX1_RGB, BOX2_RGB, BOX3);
    }
    
    public static List<BoundingBox> grayscale() {
        return Arrays.asList(BOX1_GRAYSCALE, BOX2_GRAYSCALE, BOX3);
    }

    /** Bounding box at particular point and coordinates */
    private static BoundingBox boxAt(int x, int y, int width, int height) {
        return new BoundingBox(new Point3i(x, y, 0), new Extent(width, height));
    }
}
