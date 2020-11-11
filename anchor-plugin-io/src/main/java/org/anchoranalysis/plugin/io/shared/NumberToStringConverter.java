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
