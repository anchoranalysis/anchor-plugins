package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.List;
import java.util.function.DoubleUnaryOperator;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Derives individual objects (with confidence) from a mask and associated channel of confidence values.
 * 
 * <p>The confidence value is the mean of the confidence of each individual voxel in the mask.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class DeriveObjectsFromMask {
    
    /** 
     * Splits a mask into connected-components and associates a confidence.
     * 
     * @param mask mask to split into connected-components
     * @param channel a channel, the same size as {@code mask} with a confidence-value for each voxel in a {@code mask}.
     * @param transformToConfidence transforms from the unsigned-integer found in {@code channel} to a confidence value {@code 0 <= confidence <= 1}.
     * @param shift a shift to add to the object-masks after extracting the confidence-level.  
     * @param minNumberVoxels the minimum number of voxels that must exist to form a seperate object, otherwise the voxels are ignored.
     * @return a list of objects created from the connected-components of the mask with associated confidence-values
     **/
    public static List<WithConfidence<ObjectMask>> splitIntoObjects(Mask mask, Channel channel, DoubleUnaryOperator transformToConfidence, ReadableTuple3i shift, int minNumberVoxels) throws OperationFailedException {
        Preconditions.checkArgument(mask.extent().equals(channel.extent()));
        
        ObjectsFromConnectedComponentsFactory creator = new ObjectsFromConnectedComponentsFactory(minNumberVoxels);
        
        // All the objects
        ObjectCollection objects = creator.createConnectedComponents(mask);
        
        // Associate a confidence value, by the mean-intensity of all confidence voxels in the mask
        return objects.stream().mapToList( object -> deriveConfidenceAndShift(object, channel, transformToConfidence, shift) );        
    }
    
    private static WithConfidence<ObjectMask> deriveConfidenceAndShift(ObjectMask object, Channel channel, DoubleUnaryOperator transformToConfidence, ReadableTuple3i shift) throws OperationFailedException {
        return new WithConfidence<>(object.shiftBy(shift),
            confidenceForObject(object,channel,transformToConfidence));
    }
    
    /** The mean value of all the confidence values for each voxel in the channel, translated-back */
    private static double confidenceForObject( ObjectMask object, Channel channel, DoubleUnaryOperator unscale ) throws OperationFailedException {
        Histogram histogram = HistogramFactory.createHistogramIgnoreZero(channel, object, false);
        return unscale.applyAsDouble(histogram.mean());
    }
}
