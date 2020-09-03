package org.anchoranalysis.plugin.opencv.convert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.test.ImageLoader;
import org.junit.Test;
import org.opencv.core.Mat;

/**
 * Tests conversions to and from {@link Mat}.
 * 
 * @author Owen Feehan
 *
 */
public class ConvertTest {
    
    private ImageLoader loader = new ImageLoader();

    @Test
    public void testGrayScale8Bit() throws OperationFailedException {
        testConversion( loader.carGrayscale8Bit(), false );
    }
    
    @Test
    public void testGrayScale16Bit() throws OperationFailedException {
        testConversion( loader.carGrayscale16Bit(), false );
    }
    
    @Test
    public void testRGB() throws OperationFailedException {
        testConversion( loader.carRGB(), true );
    }
    
    private void testConversion(Stack stack, boolean expectFinalStackBackedByArray) throws OperationFailedException {
        try {
            // Convert to Mat
            Mat mat = ConvertToMat.fromStack(stack);
            
            // Convert from Mat back to stack
            Stack stackCopiedBack = ConvertFromMat.toStack(mat);
            
            // Image-resolution is permitted to be different
            assertTrue("voxel by voxel equals", stack.equalsDeep(stackCopiedBack,false) );

            // We check if the buffer is backed by an array, as a proxy for whether it was created
            //   internally by Anchor or whether it came from JavaCPP's OpenCV wrapper
            // This gives an indication if we are reusing buffers as opposed to creating new memory and copying.
            assertEquals("buffer backed by array", expectFinalStackBackedByArray, isBufferBackedByArray(stackCopiedBack) );
            
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
    
    private static boolean isBufferBackedByArray(Stack stack) {
        return stack.getChannel(0).voxels().any().slice(0).buffer().hasArray();
    }
}
