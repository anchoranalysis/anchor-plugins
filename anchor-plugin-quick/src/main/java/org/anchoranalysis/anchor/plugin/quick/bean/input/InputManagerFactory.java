/* (C)2020 */
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
