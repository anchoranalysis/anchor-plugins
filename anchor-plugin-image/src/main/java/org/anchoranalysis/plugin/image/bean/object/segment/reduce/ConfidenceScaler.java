package org.anchoranalysis.plugin.image.bean.object.segment.reduce;

import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.datatype.UnsignedByteVoxelType;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Methods to scale normalizes a {@code double} confidence value to an unsigned-int range, and to reverse scale it back.
 * 
 * <p>Specifically, it maps a {@code double} {@code 0 <= confidence <= 1} 
 *  so that it is an int {@code 1 <= confidence <= 255}. And performs the reverse operation to unscale.
 *  
 * <p>The voxel-value 0 is avoided, as this is reserved for voxels that have no object-mask present.
 *  
 * <p>The mapping is a linear transformation based upon the minimum and maximum confidence values to maximize the spread
   on the available range for unsigned byte.
 * 
 * @author Owen Feehan
 *
 */
class ConfidenceScaler {

    private static final int MAX_RANGE_INT_MINUS = UnsignedByteVoxelType.MAX_VALUE_INT - 1;
    
    private double minConfidence;
    private double maxConfidence;
    private double spread;
    
    /**
     * Create a scaler for a list of elements 
     * 
     * @param elements all the existing elements with confidence
     */
    public ConfidenceScaler(List<WithConfidence<ObjectMask>> elements) {
        minConfidence = aggregateFunction(elements, DoubleStream::min);
        maxConfidence = aggregateFunction(elements, DoubleStream::max);
        // Avoid the spread being 0 by using a very tiny value if there's no confidence difference
        spread = Math.max(maxConfidence - minConfidence, 0.000001);
    }
    
    /**
     * Scales a confidence-value <b>to</b> an unsigned-byte.
     * 
     * @param confidence the confidence-value to scale {@code 0 <= confidence <= 1} 
     * @return the value scaled to unsigned-byte range (expressed as an int)
     */
    public int downscale( double confidence ) {
        double ratio = (confidence - minConfidence ) / spread;
        return 1 + (int) (ratio * MAX_RANGE_INT_MINUS);
    }
    
    /**
     * Scales a confidence-value <b>from</b> an unsigned-byte.
     * 
     * @param unsignedByteValue the value scaled to unsigned-byte range (expressed as a double)
     * @return the value in the original double-scale {@code 0 <= confidence <= 1} 
     */
    public double upscale( double unsignedByteValue ) {
        double ratio = ((unsignedByteValue-1)/MAX_RANGE_INT_MINUS) * spread; 
        return ratio + minConfidence;
    }
        
    private static double aggregateFunction(List<WithConfidence<ObjectMask>> list, Function<DoubleStream,OptionalDouble> aggregateFunction) {
        DoubleStream stream = list.stream().mapToDouble(WithConfidence::getConfidence);
        return aggregateFunction.apply(stream).getAsDouble();
    }
}
