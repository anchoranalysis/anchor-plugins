package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleUnaryOperator;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.box.BoundedList;
import org.anchoranalysis.image.extent.box.BoundingBox;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ThresholdConfidence extends ReduceElements<ObjectMask> {
    
    // START BEAN PROPERTIES
    /** The minimum confidence of an element for its object-mask to be included. */ 
    @BeanField @Getter @Setter private double minConfidence = 0.5;
    
    /** The minimum number of voxels that must exist in a connected-component to be included. */
    @BeanField @Getter @Setter private int minNumberVoxels = 5;
    // END BEAN PROPERTIES
    
    /**
     * Creates with a minimum-confidence level.
     *  
     * @param minConfidence the minimum confidence of an element for its object-mask to be included.
     */
    public ThresholdConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    @Override
    public List<WithConfidence<ObjectMask>> reduce(List<WithConfidence<ObjectMask>> elements) throws OperationFailedException {

        if (elements.isEmpty()) {
            // An empty input list produces an empty output list
            return new ArrayList<>();
        }
        
        BoundedList<WithConfidence<ObjectMask>> boundedList = new BoundedList<>(elements, withConfidence -> withConfidence.getElement().boundingBox() ); 
        
        ConfidenceScaler scaler = new ConfidenceScaler(elements);
        
        Channel channel = writeConfidenceIntoChannel(elements, boundedList.boundingBox(), scaler::downscale);
        
        Mask mask = threshold(channel.duplicate(), scaler::downscale);
        return objectsWithConfidenceFromMask(mask, channel, scaler::upscale, boundedList.boundingBox().cornerMin() );
    }
    
    /** Builds list of object-masks (with associated confidence) by taking connected-components from the threshold */
    private List<WithConfidence<ObjectMask>> objectsWithConfidenceFromMask(Mask mask, Channel channel, DoubleUnaryOperator unscale, ReadableTuple3i shift) throws OperationFailedException {
        ObjectsFromConnectedComponentsFactory creator = new ObjectsFromConnectedComponentsFactory(minNumberVoxels);
        
        // All the objects
        ObjectCollection objects = creator.createConnectedComponents(mask);
        
        // Associate a confidence value, by the mean-intensity of all confidence voxels in the mask
        return objects.stream().mapToList( object -> deriveConfidenceAndShift(object, channel, unscale, shift) );        
    }
    
    private static WithConfidence<ObjectMask> deriveConfidenceAndShift(ObjectMask object, Channel channel, DoubleUnaryOperator unscale,  ReadableTuple3i shift) throws OperationFailedException {
        return new WithConfidence<>(object.shiftBy(shift),
            confidenceForObject(object,channel,unscale));
    }
    
    /** The mean value of all the confidence values for each voxel in the channel, translated-back */
    private static double confidenceForObject( ObjectMask object, Channel channel, DoubleUnaryOperator unscale ) throws OperationFailedException {
        Histogram histogram = HistogramFactory.createHistogramIgnoreZero(channel, object, false);
        return unscale.applyAsDouble(histogram.mean());
    }
    
    private Mask threshold(Channel channel, DoubleToIntFunction normalizer) throws OperationFailedException {
        // If we pass a lower minConfidence value than appears in any element, it will
        //  be scaled to a negative value, so we must adjust to bring it to 1, otherwise non-objects will be included.
        int minConfidenceNormalized = Math.max(normalizer.applyAsInt(minConfidence), 1);
        ThresholderGlobal thresholder = new ThresholderGlobal( new Constant(minConfidenceNormalized) );
        
        return new Mask(thresholder.threshold(channel.voxels()));
    }

    private Channel writeConfidenceIntoChannel(List<WithConfidence<ObjectMask>> elements, BoundingBox boxOverall, DoubleToIntFunction convertConfidence ) {
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
