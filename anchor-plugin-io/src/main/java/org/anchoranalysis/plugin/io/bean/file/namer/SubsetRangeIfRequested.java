package org.anchoranalysis.plugin.io.bean.file.namer;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.index.IndexRange;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;

/**
 * If specified in the context, the existing name if subsetted according to an index range.
 *
 * @author Owen Feehan
 */
public class SubsetRangeIfRequested extends FileNamer {

    /** Character used to split the name into groups. */
    private static final String SPLIT_NAME_CHARACTER = "/";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FileNamer namer;
    // END BEAN PROPERTIES

    @Override
    public List<NamedFile> deriveName(Collection<File> files, FileNamerContext context) {

        List<NamedFile> namedFiles = namer.deriveName(files, context);

        if (context.getNameSubrange().isPresent()) {
            return FunctionalList.mapToList(
                    namedFiles,
                    OperationFailedException.class,
                    file ->
                            subsetFile(
                                    file,
                                    context.getNameSubrange().get(),
                                    context.getLogger().errorReporter()));
        } else {
            return namedFiles;
        }
    }

    private static NamedFile subsetFile(
            NamedFile namedFile, IndexRange range, ErrorReporter errorReporter) {
        return namedFile.mapIdentifier((name, file) -> subsetName(name, range, errorReporter));
    }

    private static String subsetName(String name, IndexRange range, ErrorReporter errorReporter) {
        List<String> groups = splitName(name);
        try {
            List<String> subset = range.extract(groups);
            return String.join(SPLIT_NAME_CHARACTER, subset);
        } catch (OperationFailedException e) {
            errorReporter.recordError(
                    SubsetRangeIfRequested.class,
                    String.format(
                            "Cancelling subset of names for %s. Reverting to original name.%n%s",
                            name, e.getMessage()));
            return name;
        }
    }

    private static List<String> splitName(String name) {
        return Arrays.asList(name.split(SPLIT_NAME_CHARACTER));
    }
}
