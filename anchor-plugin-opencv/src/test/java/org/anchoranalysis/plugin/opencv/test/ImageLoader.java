/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.anchoranalysis.plugin.opencv.test;

import java.nio.file.Path;
import org.anchoranalysis.feature.energy.EnergyStackWithoutParams;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.test.TestLoader;
import org.anchoranalysis.test.image.io.TestLoaderImage;

/**
 * Loads images used in OpenCV tests and initializes openCV if needed.
 *
 * @author Owen Feehan
 */
public class ImageLoader {

    private static final String PATH_CAR = "car.jpg";

    private static final String PATH_CAR_GRAYSCALE_8_BIT = "carGrayscale8bit.jpg";

    private static final String PATH_CAR_GRAYSCALE_16_BIT = "carGrayscale16bit.tif";

    private TestLoaderImage loader =
            new TestLoaderImage(TestLoader.createFromMavenWorkingDirectory());

    public Stack carRGB() {
        return loader.openStackFromTestPath(PATH_CAR);
    }

    public Stack carGrayscale8Bit() {
        return loader.openStackFromTestPath(PATH_CAR_GRAYSCALE_8_BIT);
    }

    public Stack carGrayscale16Bit() {
        return loader.openStackFromTestPath(PATH_CAR_GRAYSCALE_16_BIT);
    }

    public EnergyStackWithoutParams carRGBAsEnergy() {
        return new EnergyStackWithoutParams(carRGB());
    }

    public EnergyStackWithoutParams carGrayscale8BitAsEnergy() {
        return new EnergyStackWithoutParams(carGrayscale8Bit());
    }

    public EnergyStackWithoutParams carGrayscale16BitAsEnergy() {
        return new EnergyStackWithoutParams(carGrayscale16Bit());
    }

    public Path modelDirectory() {
        return loader.getLoader().getRoot();
    }
}
