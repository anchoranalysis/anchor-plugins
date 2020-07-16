/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.task;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.text.TypedValue;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class TypedValueUtilities {

    public static List<String> createArrayFromList(List<String[]> list, int index)
            throws OperationFailedException {

        List<String> out = new ArrayList<>();

        for (String[] s : list) {
            out.add(removeQuotes(s[index]));
        }
        return out;
    }

    public static List<TypedValue> createTypedArrayFromList(List<String[]> list, int index)
            throws OperationFailedException {

        List<TypedValue> out = new ArrayList<>();

        for (String[] s : list) {
            addGuessType(out, s[index]);
        }
        return out;
    }

    public static List<String> listFromArray(String[] arr) throws OperationFailedException {

        List<String> out = new ArrayList<>();

        for (String s : arr) {
            out.add(removeQuotes(s));
        }
        return out;
    }

    public static List<TypedValue> typeArray(String[] arr) throws OperationFailedException {

        List<TypedValue> out = new ArrayList<>();

        for (String s : arr) {
            addGuessType(out, s);
        }
        return out;
    }

    private static String removeQuotes(String s) throws OperationFailedException {
        if (s.charAt(0) != '\"') {
            throw new OperationFailedException("Quote character at start is missing");
        }
        if (s.charAt(s.length() - 1) != '\"') {
            throw new OperationFailedException("Quote character at end is missing");
        }
        return s.substring(1, s.length() - 1);
    }

    private static void addGuessType(List<TypedValue> out, String s)
            throws OperationFailedException {
        if (s.charAt(0) == '\"') {
            out.add(new TypedValue(removeQuotes(s)));
        } else {
            out.add(new TypedValue(s, true));
        }
    }
}
