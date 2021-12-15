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

package org.anchoranalysis.plugin.io.bean.stack.reader;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.bean.stack.reader.StackReader;
import org.anchoranalysis.image.io.stack.input.OpenedImageFile;
import org.anchoranalysis.plugin.io.bean.file.group.parser.FilePathParser;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.OpenedMultiFile;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

// Expects to be passed one file per set
// Then finds all channels and stacks from an associated regular expression
//   that is matched against files in the directory
public class MultiFileReader extends StackReader {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathParser filePathParser;

    @BeanField @DefaultInstance @Getter @Setter private StackReader stackReader;

    @BeanField @Getter @Setter private boolean recurseSubfolders = false;

    /** Search x number directories higher than file. */
    @BeanField @Getter @Setter private int navigateHigherDirectories = 0;

    /** If non-empty a regular-expression is applied to files. */
    @BeanField @AllowEmpty @Getter @Setter private String regExFile = "";

    /** If non-empty a regular-expression is applied to directories. */
    @BeanField @AllowEmpty @Getter @Setter private String regExDirectory = "";
    // END BEAN PROPERTIES

    @Override
    public OpenedImageFile openFile(Path filePath, ExecutionTimeRecorder executionTimeRecorder)
            throws ImageIOException {

        // We look at all other files in the same folder as our filepath to match our expression

        Iterator<File> fileIterator =
                FileUtils.iterateFiles(
                        folderFromFile(filePath), maybeRegExFilter(regExFile), recurseFilter());

        ParsedFilePathBag bag = new ParsedFilePathBag();

        while (fileIterator.hasNext()) {
            Optional<FileDetails> details = filePathParser.parsePath(fileIterator.next().toPath());
            if (details.isPresent()) {
                bag.add(details.get());
            }
        }

        return new OpenedMultiFile(stackReader, bag, executionTimeRecorder);
    }

    private File folderFromFile(Path filePath) {

        File dir = filePath.toFile();

        if (!filePath.toFile().isDirectory()) {
            // If we start with a file-path we first go to the parent directory
            dir = dir.getParentFile();
        }

        for (int i = 0; i < navigateHigherDirectories; i++) {
            dir = dir.getParentFile();
        }

        return dir;
    }

    private IOFileFilter recurseFilter() {
        return recurseSubfolders ? maybeRegExFilter(regExDirectory) : null;
    }

    private static IOFileFilter maybeRegExFilter(String regEx) {
        if (!regEx.isEmpty()) {
            return new RegexFileFilter(regEx);
        } else {
            return TrueFileFilter.INSTANCE;
        }
    }
}
