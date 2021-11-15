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

package org.anchoranalysis.plugin.io.bean.file.namer.patternspan;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.file.NamedFile;
import org.anchoranalysis.plugin.io.bean.input.files.NamedFiles;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ExtractVariableSpanForList {

    /**
     * Extract {@link NamedFiles} from the files by naming via the extracted pattern.
     *
     * <p>If any of the identifiers are empty, then a string (constant portion) is appended to all
     * names so none are empty.
     *
     * @param files files, which must be the same files, in the same order as the pattern in {@code extractVariableSpan}.
     * @param extractVariableSpan extracted-pattern.
     * @return a list corresponding to {@code files} but with names associated.
     */
    public static List<NamedFile> listExtract(
            List<File> files, ExtractVariableSpan extractVariableSpan) {

         List<NamedFile> listMaybeEmpty = FunctionalList.mapToListWithIndex(
                files, (file, index) -> namedFileFor(extractVariableSpan, index, file)
         );
                
        if (hasAnyEmptyIdentifier(listMaybeEmpty)) {
            String prependStr =
                    extractLastComponent(
                            extractVariableSpan.extractConstantElementBeforeSpanPortion());
            return listPrepend(prependStr, listMaybeEmpty);
        } else {
            return listMaybeEmpty;
        }
    }
    
    private static NamedFile namedFileFor(ExtractVariableSpan extractVariableSpan, int fileIndex, File file) {
        return new NamedFile(extractVariableSpan.extractSpanPortionFor(fileIndex), file);
    }

    private static List<NamedFile> listPrepend(String prefix, List<NamedFile> list) {
        return mapNamesOnList(list, (name, file) -> prependName(prefix, name));
    }

    private static List<NamedFile> mapNamesOnList(
            Collection<NamedFile> collection, BiFunction<String, File, String> changeName) {
        return FunctionalList.mapToList(collection, df -> df.mapIdentifier(changeName));
    }

    private static String prependName(String prefix, String name) {
        return FilePathToUnixStyleConverter.toStringUnixStyle(prefix + name).trim();
    }

    /** Whether any identifier is missing... */
    private static boolean hasAnyEmptyIdentifier(List<NamedFile> list) {
        return list.stream().filter(file -> file.getIdentifier().isEmpty()).count() > 0;
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
