package org.anchoranalysis.plugin.opencv.convert;

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
        testConversion( loader.carGrayscale8Bit() );
    }
    
    @Test
    public void testGrayScale16Bit() throws OperationFailedException {
        testConversion( loader.carGrayscale16Bit() );
    }
    
    @Test
    public void testRGB() throws OperationFailedException {
        testConversion( loader.carRGB() );
    }
    
    private void testConversion(Stack stack) throws OperationFailedException {
        try {
            // Convert to Mat
            Mat mat = ConvertToMat.fromStack(stack);
            
            // Convert from Mat back to stack
            Stack stackCopiedBack = ConvertFromMat.toStack(mat);
            
            // Image-resolution is permitted to be different
            assertTrue("voxel by voxel equals", stack.equalsDeep(stackCopiedBack,false) );
            
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
