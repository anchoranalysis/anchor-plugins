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

package org.anchoranalysis.plugin.io.bean.file.group;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.io.bean.channel.ChannelMapCreator;
import org.anchoranalysis.image.io.bean.stack.reader.InputManagerWithStackReader;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.InputsWithDirectory;
import org.anchoranalysis.io.input.bean.InputManagerParameters;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileInput;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;
import org.anchoranalysis.plugin.io.bean.file.group.check.CheckParsedFilePathBag;
import org.anchoranalysis.plugin.io.bean.file.group.parser.FilePathParser;
import org.anchoranalysis.plugin.io.bean.file.namer.LastDirectories;
import org.anchoranalysis.plugin.io.bean.input.files.NamedFiles;
import org.anchoranalysis.plugin.io.bean.stack.reader.MultiFileReader;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.OpenedMultiFile;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;

/**
 * An {@link InputManagerWithStackReader} that can unify separate images in separate files to form a
 * single {@link Stack}.
 *
 * <p>A time-series of {@link Stack}s may also be formed.
 *
 * <p>The criteria on how to unify is defined by a pattern in the file path (via regular
 * expressions).
 *
 * <p>A regular expression is applied on the set of input file paths, to identify one or more
 * groups:
 *
 * <ol>
 *   <li>One group is the <b>image key</b> (something that uniquely identifies each image).
 *   <li>One group is the <b>slice-identifier</b> (identifies the z slice, must be positive integer.
 *   <li>One group is the <b>channel-identifier</b> (identifies the channel, must be positive
 *       integer).
 * </ol>
 *
 * <p>For each image key, an image is loaded using the slice and channel-identifiers.
 *
 * <p>Integer numbers are simply loaded in ascending numerical order. So gaps are allowed, and
 * starting numbers are irrelevant.
 *
 * <p>It is more powerful than {@link MultiFileReader}, which expects only one image per directory.
 * This class allows multiple images per directory and only performs a single glob for filenames.
 *
 * @author Owen Feehan
 */
public class GroupFiles extends InputManagerWithStackReader<NamedChannelsInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private NamedFiles fileInput;

    @BeanField @Getter @Setter private FilePathParser pathParser;

    @BeanField @Getter @Setter private boolean requireAllFilesMatch = false;

    @BeanField @Getter @Setter private ChannelMapCreator imgChannelMapCreator;

    @BeanField @Getter @Setter private FileNamer namer = new LastDirectories(2);

    /**
     * Imposes a condition on each parsed-file-path-bag which must be-fulfilled if a file is to be
     * included.
     */
    @BeanField @OptionalBean @Getter @Setter private CheckParsedFilePathBag checkParsedFilePathBag;
    // END BEAN PROPERTIES

    @Override
    public InputsWithDirectory<NamedChannelsInput> inputs(InputManagerParameters parameters)
            throws InputReadFailedException {

        InputsWithDirectory<FileInput> inputs = fileInput.inputs(parameters);

        GroupFilesMap map = new GroupFilesMap();

        // Iterate through each file, match against the reg-exp and populate a hash-map
        Iterator<FileInput> iterator = inputs.iterator();
        while (iterator.hasNext()) {

            FileInput input = iterator.next();

            Optional<FileDetails> details = pathParser.parsePath(input.getFile().toPath());
            if (details.isPresent()) {
                map.add(pathParser.getKey(), details.get());
            } else {
                if (requireAllFilesMatch) {
                    throw new InputReadFailedException(
                            String.format(
                                    "File %s did not match parser", input.getFile().toPath()));
                }
            }
        }
        return inputs.withInputs(
                listFromMap(map, parameters.getExecutionTimeRecorder(), parameters.getLogger()));
    }

    private List<NamedChannelsInput> listFromMap(
            GroupFilesMap map, ExecutionTimeRecorder executionTimeRecorder, Logger logger)
            throws InputReadFailedException {

        List<File> files = new ArrayList<>();
        List<OpenedMultiFile> openedFiles = new ArrayList<>();

        // Process the hash-map by key
        for (String key : map.keySet()) {
            ParsedFilePathBag bag = map.get(key);

            // If we have a condition to check against
            if (checkParsedFilePathBag == null || checkParsedFilePathBag.accept(bag)) {
                files.add(Paths.get(key).toFile());
                openedFiles.add(new OpenedMultiFile(getStackReader(), bag, executionTimeRecorder));
            }
        }

        List<NamedFile> namedFiles = namer.deriveNameUnique(files, new FileNamerContext(logger));
        return zipIntoGrouping(namedFiles, openedFiles);
    }

    private List<NamedChannelsInput> zipIntoGrouping(
            List<NamedFile> files, List<OpenedMultiFile> openedFiles) {

        Iterator<NamedFile> iterator1 = files.iterator();
        Iterator<OpenedMultiFile> iterator2 = openedFiles.iterator();

        List<NamedChannelsInput> result = new ArrayList<>();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            result.add(
                    new GroupingInput(
                            iterator1.next().getFile().toPath(),
                            iterator2.next(),
                            imgChannelMapCreator));
        }
        return result;
    }
}
