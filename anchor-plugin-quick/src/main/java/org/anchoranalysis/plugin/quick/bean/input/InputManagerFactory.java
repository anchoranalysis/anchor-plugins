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

package org.anchoranalysis.plugin.quick.bean.input;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.io.input.bean.InputManager;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithDirectory;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileInput;
import org.anchoranalysis.plugin.io.bean.file.provider.Rooted;
import org.anchoranalysis.plugin.io.bean.input.files.NamedFiles;
import org.anchoranalysis.plugin.io.bean.input.filter.FilterCSVColumn;
import org.anchoranalysis.plugin.quick.bean.input.filepathappend.MatchedAppendCsv;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class InputManagerFactory {

    // Like createFiles, but maybe also wraps it in a filter
    public static InputManager<FileInput> createFiles(
            String rootName,
            FilesProviderWithDirectory files,
            FileNamer namer,
            String regex,
            MatchedAppendCsv filterFilesCsv)
            throws BeanMisconfiguredException {

        InputManager<FileInput> filesCreated = createFiles(rootName, files, namer);

        if (filterFilesCsv == null) {
            return filesCreated;
        }

        FilterCSVColumn<FileInput> filterManager = new FilterCSVColumn<>();
        filterManager.setInput(filesCreated);
        filterManager.setMatch(filterFilesCsv.getMatch());
        filterManager.setCsvFilePath(
                filterFilesCsv.getAppendCsv().createPathDeriver(rootName, regex).getItem());
        return filterManager;
    }

    /**
     * Creates an input-manager of type {@link FileInput}.
     *
     * @param rootName if non-empty a {@link Rooted} is used
     * @param files provider of files
     * @param namer assigns each file a unique compact name
     * @return
     */
    private static InputManager<FileInput> createFiles(
            String rootName, FilesProviderWithDirectory files, FileNamer namer) {
        return new NamedFiles(createMaybeRootedFileProvider(rootName, files), namer);
    }

    private static FilesProvider createMaybeRootedFileProvider(
            String rootName, FilesProviderWithDirectory provider) {
        if (!rootName.isEmpty()) {
            return createRootedFileProvider(rootName, provider);
        } else {
            return provider;
        }
    }

    private static FilesProvider createRootedFileProvider(
            String rootName, FilesProviderWithDirectory provider) {
        Rooted fileSet = new Rooted();
        fileSet.setRootName(rootName);
        fileSet.setFiles(provider);
        fileSet.setDisableDebugMode(true);
        return fileSet;
    }
}
