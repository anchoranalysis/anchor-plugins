/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.file.namer;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.index.range.IndexRangeNegative;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;

/**
 * If specified in the context, the existing name if subsetted according to an index range.
 *
 * <p>The existing name is split into groups by a deliminator (by default a forward/slash) to
 * provide groups for the subsetting.
 *
 * @author Owen Feehan
 */
public class SubsetRangeIfRequested extends FileNamer {

    // START BEAN PROPERTIES
    /** The namer that is called to provide names that are subsetted. */
    @BeanField @Getter @Setter private FileNamer namer;

    /** Character used to split the name into groups. */
    @BeanField @Getter @Setter private String delimiter = "/";

    // END BEAN PROPERTIES

    @Override
    public List<NamedFile> deriveName(List<File> files, FileNamerContext context) {

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

    private NamedFile subsetFile(
            NamedFile namedFile, IndexRangeNegative range, ErrorReporter errorReporter) {
        return namedFile.mapIdentifier((name, file) -> subsetName(name, range, errorReporter));
    }

    private String subsetName(String name, IndexRangeNegative range, ErrorReporter errorReporter) {
        List<String> groups = splitName(name);
        try {
            List<String> subset = range.extract(groups);
            return String.join(delimiter, subset);
        } catch (OperationFailedException e) {
            errorReporter.recordError(
                    SubsetRangeIfRequested.class,
                    String.format(
                            "Cancelling subset of names for %s. Reverting to original name.%n%s",
                            name, e.getMessage()));
            return name;
        }
    }

    private List<String> splitName(String name) {
        return Arrays.asList(name.split(delimiter));
    }
}
