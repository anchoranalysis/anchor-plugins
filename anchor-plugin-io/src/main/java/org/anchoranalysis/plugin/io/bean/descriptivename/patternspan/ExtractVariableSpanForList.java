/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename.patternspan;

import java.io.File;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.io.filepath.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractVariableSpanForList {

    /**
     * Extract Descriptive-Files from the files via the extracted pattern
     *
     * <p>If any of descriptive-names are blank, then add on a bit of the constant portion to all
     * names to make it non-blank
     *
     * @param files files
     * @param extractVariableSpan extracted-pattern
     * @return
     */
    public static List<DescriptiveFile> listExtract(
            Collection<File> files, ExtractVariableSpan extractVariableSpan) {
        List<DescriptiveFile> listMaybeEmpty = listExtractMaybeEmpty(files, extractVariableSpan);

        if (hasAnyEmptyDescriptiveName(listMaybeEmpty)) {
            String prependStr =
                    extractLastComponent(
                            extractVariableSpan.extractConstantElementBeforeSpanPortion());
            return listPrepend(prependStr, listMaybeEmpty);
        } else {
            return listMaybeEmpty;
        }
    }

    private static List<DescriptiveFile> listPrepend(
            String prependStr, List<DescriptiveFile> list) {
        return FunctionalList.mapToList(
                list,
                df ->
                        new DescriptiveFile(
                                df.getFile(),
                                FilePathToUnixStyleConverter.toStringUnixStyle(
                                                prependStr + df.getDescriptiveName())
                                        .trim()));
    }

    private static boolean hasAnyEmptyDescriptiveName(List<DescriptiveFile> list) {
        return list.stream().filter(df -> df.getDescriptiveName().isEmpty()).count() > 0;
    }

    private static List<DescriptiveFile> listExtractMaybeEmpty(
            Collection<File> files, ExtractVariableSpan extractVariableSpan) {
        return FunctionalList.mapToList(
                files,
                file -> new DescriptiveFile(file, extractVariableSpan.extractSpanPortionFor(file)));
    }

    private static String extractLastComponent(String str) {

        str = FilePathToUnixStyleConverter.toStringUnixStyle(str);

        if (str.length() == 1) {
            return str;
        }

        // Find the index of the last forward-slash (or second-last if the final character is a
        // forward-slash)
        String strMinusOne = str.substring(0, str.length() - 1);
        int finalIndex = strMinusOne.lastIndexOf('/');
        return str.substring(finalIndex + 1);
    }
}
