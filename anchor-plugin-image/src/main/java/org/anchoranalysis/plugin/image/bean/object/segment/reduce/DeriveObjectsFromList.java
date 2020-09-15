package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.List;
import java.util.function.DoubleToIntFunction;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.box.BoundedList;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Merges individual objects (with confidence) together if spatially adjacent.
 * 
 * <p>The confidence of each voxel in an object is projected onto a channel (always taking the max
 * if the voxel belongs to multiple objects).
 * 
 * <p>This channel is then thresholded, and split into connected components, with a confidence value
 * inferred for the new objects.
 * 
 * <p>The confidence value is the mean of the confidence of each individual voxel in the mask.
 * 
 * @author Owen Feehan
 *
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
class DeriveObjectsFromList {

    public static List<WithConfidence<ObjectMask>> deriveObjects(BoundedList<WithConfidence<ObjectMask>> boundedList, List<WithConfidence<ObjectMask>> elements, double minConfidence, int minNumberVoxels) throws OperationFailedException {
        ConfidenceScaler scaler = new ConfidenceScaler(elements);
        
        Channel channel = writeConfidenceIntoChannel(elements, boundedList.boundingBox(), scaler::downscale);
        
        Mask mask = threshold(channel.duplicate(), minConfidence, scaler::downscale);
        return DeriveObjectsFromMask.splitIntoObjects(mask, channel, scaler::upscale, boundedList.boundingBox().cornerMin(), minNumberVoxels);        
    }
    
    private static Mask threshold(Channel channel, double minConfidence, DoubleToIntFunction convertConfidence) throws OperationFailedException {
        // If we pass a lower minConfidence value than appears in any element, it will
        //  be scaled to a negative value, so we must adjust to bring it to 1, otherwise non-objects will be included.
        int minConfidenceNormalized = Math.max(convertConfidence.applyAsInt(minConfidence), 1);
        ThresholderGlobal thresholder = new ThresholderGlobal( new Constant(minConfidenceNormalized) );
        
        return new Mask(thresholder.threshold(channel.voxels()));
    }

    private static Channel writeConfidenceIntoChannel(List<WithConfidence<ObjectMask>> elements, BoundingBox boxOverall, DoubleToIntFunction convertConfidence ) {
        Dimensions dimensions = new Dimensions(boxOverall.extent());
        Channel channel = ChannelFactory.instance().get(UnsignedByteVoxelType.INSTANCE).createEmptyInitialised(dimensions);
        
        for( WithConfidence<ObjectMask> withConfidence : elements ) {
            int confidenceAsInt = convertConfidence.applyAsInt(withConfidence.getConfidence());
            
            // Assign a value to the voxels only if it is greater than the existing-value
            channel.assignValue(confidenceAsInt).toObjectIf(withConfidence.getElement().relativeMaskTo(boxOverall), voxelValue -> voxelValue==0 );
        }
        
        return channel;
    }
}
