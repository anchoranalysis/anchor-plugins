/*-
 * #%L
 * anchor-plugin-quick
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

package org.anchoranalysis.anchor.plugin.quick.bean.input;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.input.file.Files;
import org.anchoranalysis.plugin.io.bean.input.filter.FilterCsvColumn;
import org.anchoranalysis.plugin.io.bean.provider.file.Rooted;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InputManagerFactory {

    // Like createFiles, but maybe also wraps it in a filter
    public static InputManager<FileInput> createFiles(
            String rootName,
            FileProviderWithDirectory fileProvider,
            DescriptiveNameFromFile descriptiveNameFromFile,
            String regex,
            MatchedAppendCsv filterFilesCsv)
            throws BeanMisconfiguredException {

        InputManager<FileInput> files =
                createFiles(rootName, fileProvider, descriptiveNameFromFile);

        if (filterFilesCsv == null) {
            return files;
        }

        FilterCsvColumn<FileInput> filterManager = new FilterCsvColumn<>();
        filterManager.setInput(files);
        filterManager.setMatch(filterFilesCsv.getMatch());
        filterManager.setCsvFilePath(
                filterFilesCsv.getAppendCsv().createFilePathGenerator(rootName, regex).getItem());
        return filterManager;
    }

    /**
     * Creates a Files
     *
     * @param rootName if non-empty a RootedFileProvier is used
     * @param fileProvider fileProvider
     * @param descriptiveNameFromFile descriptiveName
     * @return
     */
    private static InputManager<FileInput> createFiles(
            String rootName,
            FileProviderWithDirectory fileProvider,
            DescriptiveNameFromFile descriptiveNameFromFile) {
        Files files = new Files();
        files.setFileProvider(createMaybeRootedFileProvider(rootName, fileProvider));
        files.setDescriptiveNameFromFile(descriptiveNameFromFile);
        return files;
    }

    private static FileProvider createMaybeRootedFileProvider(
            String rootName, FileProviderWithDirectory fileProvider) {
        if (rootName != null && !rootName.isEmpty()) {
            return createRootedFileProvider(rootName, fileProvider);
        } else {
            return fileProvider;
        }
    }

    private static FileProvider createRootedFileProvider(
            String rootName, FileProviderWithDirectory fileProvider) {
        Rooted fileSet = new Rooted();
        fileSet.setRootName(rootName);
        fileSet.setFileProvider(fileProvider);
        fileSet.setDisableDebugMode(true);
        return fileSet;
    }
}
