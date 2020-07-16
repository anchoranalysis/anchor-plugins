/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.descriptivename.patternspan;

import com.owenfeehan.pathpatternfinder.PathPatternFinder;
import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

/**
 * Finds a pattern in the descriptive name, and uses the region from the first variable to the
 * last-variable as the descriptive-name
 *
 * @author Owen Feehan
 */
public class PatternSpan extends DescriptiveNameFromFile {

    @Override
    public List<DescriptiveFile> descriptiveNamesFor(
            Collection<File> files, String elseName, Logger logger) {

        // Convert to list
        List<Path> paths = listConvertToPath(files);

        if (paths.size() <= 1) {
            // Everything's a constant, so there must only be a single file. Return the file-name.
            return listExtractFileName(files);
        }

        // For now, hard-coded case-insensitivity here, and in ExtractVariableSpan
        // TODO consider making it optional in both places
        Pattern pattern = PathPatternFinder.findPatternPath(paths, IOCase.INSENSITIVE);

        assert hasAtLeastOneVariableElement(pattern);

        return ExtractVariableSpanForList.listExtract(
                files, new SelectSpanToExtract(pattern).createExtracter(elseName));
    }

    private static boolean hasAtLeastOneVariableElement(Pattern pattern) {
        for (PatternElement e : pattern) {
            if (!e.hasConstantValue()) {
                return true;
            }
        }
        return false;
    }

    private static List<DescriptiveFile> listExtractFileName(Collection<File> files) {
        return FunctionalList.mapToList(
                files, file -> new DescriptiveFile(file, extensionlessNameFromFile(file)));
    }

    // Convert all Files to Path
    private static List<Path> listConvertToPath(Collection<File> files) {
        return FunctionalList.mapToList(files, File::toPath);
    }

    // The file-name without an extension
    private static String extensionlessNameFromFile(File file) {
        return FilenameUtils.removeExtension(file.getName());
    }
}
