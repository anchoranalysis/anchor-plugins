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

package org.anchoranalysis.plugin.opencv.bean.feature;

import java.util.function.ToIntFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.bean.spatial.SizeXY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DivideUtilities {

    /**
     * Integer division of one extracted value by another extracted value.
     *
     * <p>The values are extracted respectively from a instance of {@link SizeXY}.
     *
     * @param dividend the number that is divided.
     * @param divisor what {@code dividend} is divided by.
     * @param extract the function to extract an integer value from {@link SizeXY}.
     * @return the result of the integer division.
     */
    public static int divide(SizeXY dividend, SizeXY divisor, ToIntFunction<SizeXY> extract) {
        return extract.applyAsInt(dividend) / extract.applyAsInt(divisor);
    }

    public static void checkDivisibleBy(
            SizeXY biggerNumber, SizeXY divisor, String biggerText, String divisorText)
            throws FeatureCalculationException {
        if (!isDivisibleBy(biggerNumber.getWidth(), divisor.getWidth())) {
            throw notDivisibleException(biggerText, divisorText, "width");
        }

        if (!isDivisibleBy(biggerNumber.getHeight(), divisor.getHeight())) {
            throw notDivisibleException(biggerText, divisorText, "height");
        }
    }

    public static FeatureCalculationException notDivisibleException(
            String biggerText, String divisorText, String propertyText) {
        return new FeatureCalculationException(
                String.format(
                        "%s %s must be divisible by %s %s.",
                        biggerText, propertyText, divisorText, propertyText));
    }

    public static int ceilDiv(double toDivide, int divisor) {
        return (int) Math.ceil(toDivide / divisor);
    }

    /** Is a big-number divisible by the divider? */
    private static boolean isDivisibleBy(int bigNumber, int divider) {
        return (bigNumber % divider) == 0;
    }
}
