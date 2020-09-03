package org.anchoranalysis.plugin.opencv.test;

import java.nio.file.Path;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.plugin.opencv.CVInit;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImageIO;

/**
 * Loads images used in OpenCV tests and initializes openCV if needed.
 * 
 * @author Owen Feehan
 *
 */
public class ImageLoader {

    static {
        CVInit.alwaysExecuteBeforeCallingLibrary();
    }
    
    private static final String PATH_CAR = "car.jpg";
    
    private static final String PATH_CAR_GRAYSCALE_8_BIT = "carGrayscale8bit.jpg";
    
    private static final String PATH_CAR_GRAYSCALE_16_BIT = "carGrayscale16bit.tif";
    
    private TestLoaderImageIO testLoader = new TestLoaderImageIO(TestLoader.createFromMavenWorkingDirectory());

    public Stack carRGB() {
        return testLoader.openStackFromTestPath(PATH_CAR);
    }
    
    public Stack carGrayscale8Bit() {
        return testLoader.openStackFromTestPath(PATH_CAR_GRAYSCALE_8_BIT);
    }
    
    public Stack carGrayscale16Bit() {
        return testLoader.openStackFromTestPath(PATH_CAR_GRAYSCALE_16_BIT);
    }
    
    public EnergyStackWithoutParams carRGBAsEnergy() {
        return new EnergyStackWithoutParams(carRGB());
    }
    
    public Path modelDirectory() {
        return testLoader.getTestLoader().getRoot();
    }
}
