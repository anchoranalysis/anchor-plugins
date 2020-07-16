/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename.patternspan;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.apache.commons.io.IOCase;

/**
 * Extracts the a "spanning part" of the pattern from the string.
 *
 * <p>The spanning part should contain all variable elements, and optionally adjacent (or in
 * between) constant elements
 *
 * @author Owen Feehan
 */
class ExtractVariableSpan {

    private static Logger log = Logger.getLogger(ExtractVariableSpan.class.getName());

    private Pattern pattern;
    private String elseName;
    private int indexSpanStart;
    private int indexSpanEnd;

    /**
     * Constructor
     *
     * @param pattern the pattern used to extract the spanning element
     * @param elseName a constant used if failure occurs automatically extracting a descriptive-name
     * @param indexSpanStart the index of the first element that should be included in the
     *     spanning-part
     * @param indexSpanEnd the index of the last element that should be included in the
     *     spanning-part
     */
    public ExtractVariableSpan(
            Pattern pattern, String elseName, int indexSpanStart, int indexSpanEnd) {
        Preconditions.checkArgument(indexSpanEnd >= indexSpanStart);
        this.pattern = pattern;
        this.elseName = elseName;
        this.indexSpanStart = indexSpanStart;
        this.indexSpanEnd = indexSpanEnd;
    }

    /**
     * The right-most constant element before the span portion begins
     *
     * @return a string describing this constant element
     */
    public String extractConstantElementBeforeSpanPortion() {
        // Find all the constant portions before the empty string
        // Replace any constants from the left-hand-side with empty strings

        if (indexSpanStart == 0) {
            return elseName;
        }

        PatternElement element = pattern.get(indexSpanStart - 1);

        assert (element.hasConstantValue());

        return PatternUtilities.constantElementAsString(element);
    }

    /**
     * Extracts the spanning-portion from a particular file, ensuring it has UNIX-style path
     *
     * @param file
     * @return the spanning-portion of the file-path.
     */
    public String extractSpanPortionFor(File file) {
        return FilePathToUnixStyleConverter.toStringUnixStyle(
                extractVariableSpanUnnormalized(file));
    }

    private String extractVariableSpanUnnormalized(File file) {
        try {
            return extractVariableSpanWithError(file);
        } catch (OperationFailedException e) {
            log.log(Level.WARNING, "Cannot extract a variable", e);
            return elseName;
        }
    }

    private String extractVariableSpanWithError(File file) throws OperationFailedException {
        String path = file.toPath().toString();
        return trimConstantElementsFromBothSides(path, IOCase.INSENSITIVE);
    }

    private String trimConstantElementsFromBothSides(String str, IOCase ioCase)
            throws OperationFailedException {
        String[] fittedElements = pattern.fitAgainst(str, ioCase);

        if (fittedElements == null) {
            throw new OperationFailedException(
                    String.format("Cannot match pattern %s against %s", pattern, str));
        }

        assert (fittedElements.length == pattern.size());

        // Replace any constants from the left-hand-side with empty strings
        for (int i = 0; i < indexSpanStart; i++) {
            assert (pattern.get(i).hasConstantValue());
            fittedElements[i] = "";
        }

        // Replace any constants from the right-hand-side with empty strings
        for (int i = (fittedElements.length - 1); i > indexSpanEnd; i--) {
            fittedElements[i] = "";
        }

        // Combine all strings to form the name
        return combineStr(fittedElements);
    }

    private static String combineStr(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
        }
        return sb.toString();
    }
}
