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

package org.anchoranalysis.plugin.io.bean.descriptivename;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.descriptivename.FileNamer;
import org.anchoranalysis.io.input.files.NamedFile;
import org.apache.commons.io.FilenameUtils;

/**
 * Removes extensions from the descriptive-name (but not from the file) AND only if the extension
 * hasn't already been removed upstream.
 *
 * <p>As an exception, the extension can be retained if there is more than one file with the same
 * descriptive-name
 *
 * <p>To check if the extension has already been removed upstream, a check occurs if the path ends
 * with the descriptive-name
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class RemoveExtensions extends FileNamer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FileNamer namer;

    /**
     * Keeps the extension if the file-name (without the extension) becomes duplicated with another
     */
    @BeanField @Getter @Setter private boolean preserveExtensionIfDuplicate = true;
    // END BEAN PROPERTIES

    public RemoveExtensions(FileNamer namer) {
        this.namer = namer;
    }

    @Override
    public List<NamedFile> deriveName(
            Collection<File> files, String elseName, Logger logger) {

        List<NamedFile> df = namer.deriveName(files, elseName, logger);

        if (preserveExtensionIfDuplicate) {
            return considerDuplicates(df, elseName);
        } else {
            return removeExtension(df, elseName);
        }
    }

    private static List<NamedFile> considerDuplicates(
            List<NamedFile> df, String elseName) {
        List<NamedFile> dfWithoutExt = removeExtension(df, elseName);

        // A count for each file, based upon the uniqueness of the description
        Map<String, Long> countedWithoutExt =
                dfWithoutExt.stream()
                        .collect(
                                Collectors.groupingBy(
                                        NamedFile::getName,
                                        Collectors.counting()));

        return listMaybeWithExtension(df, dfWithoutExt, countedWithoutExt);
    }

    /**
     * Creates a new list selecting either the version with an extension (if there is more than 1
     * entry) or without an extension (otherwise)
     *
     * @param listWith descriptive-names with the extension
     * @param listWithout descriptive-names without the extension (in identical order to listWith)
     * @param countWithout a corresponding count for each entry in listWithout
     * @return a list in the same order, selecting either the corresponding entry from listWith or
     *     listWithout depending on the count value
     */
    private static List<NamedFile> listMaybeWithExtension(
            List<NamedFile> listWith,
            List<NamedFile> listWithout,
            Map<String, Long> countWithout) {

        List<NamedFile> out = new ArrayList<>();

        // Now we iterate through both, and if the count is more than 1, then the extension is kept
        for (int i = 0; i < listWith.size(); i++) {

            NamedFile without = listWithout.get(i);
            long count = countWithout.get(without.getName());

            out.add(count > 1 ? listWith.get(i) : without);
        }
        return out;
    }

    private static List<NamedFile> removeExtension(List<NamedFile> files, String elseName) {
        return FunctionalList.mapToList(
                files, file -> maybeRemoveExtension(file, elseName));
    }

    private static NamedFile maybeRemoveExtension(NamedFile file, String elseName) {
        String maybeExtensionRemoved =
                maybeRemoveExtensionForwardSlashes(file.getName(), file.getPath());
        if (maybeExtensionRemoved.isEmpty()) {
            maybeExtensionRemoved = elseName;
        }
        return new NamedFile(maybeExtensionRemoved, file.getFile());
    }

    private static String maybeRemoveExtensionForwardSlashes(String nameLikePath, Path path) {

        // If the path doesn't end with its derived name, we don't remove any extension
        //  as it's assumed that this has already occurred, and it could be dangerous
        //  to remove anything more.
        String pathForwardSlash = FilePathToUnixStyleConverter.toStringUnixStyle(path.normalize());
        if (!pathForwardSlash.endsWith(nameLikePath)) {
            // Early exit as the descriptive-name doesn't have the same ending as path
            return nameLikePath;
        }

        return FilePathToUnixStyleConverter.toStringUnixStyle(removeExtension(nameLikePath));
    }

    private static String removeExtension(String nameLikePath) {
        Path path = Paths.get(nameLikePath);
        Path fileNamePart = path.getFileName();
        String fileNamePartRemoved = FilenameUtils.removeExtension(fileNamePart.toString());

        // Trim any white space from either side before resolving
        fileNamePartRemoved = fileNamePartRemoved.trim();

        if (path.getParent() != null) {
            return path.getParent().resolve(fileNamePartRemoved).toString();
        } else {
            return fileNamePartRemoved;
        }
    }
}
