/*-
 * #%L
 * anchor-plugin-io
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
package org.anchoranalysis.plugin.io.shared;

/**
 * Converts a number to a strings of constant width by padding with leading zeros.
 * 
 * @author Owen Feehan
 *
 */
public class NumberToStringConverter {

    /** The format writing for writing names, determining how many leading zeros to ensure a constant width. */
    private String formatString;
    
    /**
     * Creates with a constant width just enough to be able to represent {@code maxNumberToRepresent}.
     * 
     * @param maxNumberToRepresent the maximimal number that needs to be converted.
     */
    public NumberToStringConverter(int maxNumberToRepresent) {
        formatString = createFormatStringForMaxNumber(maxNumberToRepresent);
    }

    /**
     * Converts {@code number} to a string, padding with leading zeros if necessary to ensure the desired width.
     * 
     * @param number the number to convert to a string
     * @return the string representation of {@code number}, maybe padded with leading zeros 
     */
    public String convert(int number) {
        return String.format(formatString, number);
    }

    private static String createFormatStringForMaxNumber(int maxNumberToRepresent) {
        int maxNumberDigits = (int) Math.ceil(Math.log10(maxNumberToRepresent));

        if (maxNumberDigits > 0) {
            return "%0" + maxNumberDigits + "d";
        } else {
            return "%d";
        }
    }
}
