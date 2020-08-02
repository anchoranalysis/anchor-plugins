package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import java.util.Collection;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Derives a minimally-sized extent so that all objects in a collection fit inside
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class ExtentToFitBoundingBoxes {
    
    /**
     * Derives an extent that minimally fits all bounding-boxes in the stream
     * <p>
     * Both corners of the bounding-box must fit inside.
     * <p>
     * The fit is as tight as possible.
     * 
     * @param boundingBoxes stream of bounding-boxes
     * @return an extent that fits the bounding-boxes
     */
    public static Extent derive(Stream<BoundingBox> boundingBoxes) {
        
        List<ReadableTuple3i> cornersMax = boundingBoxes.map(BoundingBox::calcCornerMax).collect( Collectors.toList() );
        
        return new Extent(
           maxDimensionValue(cornersMax, ReadableTuple3i::getX) + 1,
           maxDimensionValue(cornersMax, ReadableTuple3i::getY) + 1,
           maxDimensionValue(cornersMax, ReadableTuple3i::getZ) + 1
        );
    }
    
    private static int maxDimensionValue(Collection<ReadableTuple3i> cornersMax, ToIntFunction<ReadableTuple3i> valueForDimension ) {
        return cornersMax.stream().mapToInt(valueForDimension).max().getAsInt();
    }
}
