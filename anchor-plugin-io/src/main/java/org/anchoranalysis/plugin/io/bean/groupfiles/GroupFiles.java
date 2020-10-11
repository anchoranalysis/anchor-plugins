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

package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.io.bean.channel.map.ChannelMap;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.exception.AnchorIOException;
import org.anchoranalysis.io.input.DescriptiveFile;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.io.bean.groupfiles.check.CheckParsedFilePathBag;
import org.anchoranalysis.plugin.io.bean.groupfiles.parser.FilePathParser;
import org.anchoranalysis.plugin.io.bean.input.file.Files;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;

/**
 * An input-manager that can group together files to form stacks or time-series based on finding
 * patterns in the file path (via regular expressions)
 *
 * <p>The manager applies a regular expression on a set of input file paths, and identifies one or
 * more groups: One group is the image key (something that uniquely identifies each image) One group
 * is the slice-identifier (identifies the z slice, must be positive integer) One group is the
 * channel-identifier (identifies the channel, must be positive integer)
 *
 * <p>For each image key, an image is loaded using the slice and channel-identifiers.
 *
 * <p>Integer numbers are simply loaded in ascending numerical order. So gaps are allowed, and
 * starting numbers are irrelevant.
 *
 * <p>It is more powerful than MultiFileReader, which expects only one image per folder. This class
 * allows multiple images per folder and only performs a single glob for filenames
 *
 * @author Owen Feehan
 */
public class GroupFiles extends InputManager<NamedChannelsInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Files fileInput;

    @BeanField @DefaultInstance @Getter @Setter private RasterReader rasterReader;

    @BeanField @Getter @Setter private FilePathParser filePathParser;

    @BeanField @Getter @Setter private boolean requireAllFilesMatch = false;

    @BeanField @Getter @Setter private ChannelMap imgChannelMapCreator;

    @BeanField @Getter @Setter
    private DescriptiveNameFromFile descriptiveName = new LastFolders(2);

    /**
     * Imposes a condition on each parsedFilePathBag which must be-fulfilled if a file is to be
     * included
     */
    @BeanField @OptionalBean @Getter @Setter private CheckParsedFilePathBag checkParsedFilePathBag;
    // END BEAN PROPERTIES

    @Override
    public List<NamedChannelsInput> inputs(InputManagerParams params) throws AnchorIOException {

        GroupFilesMap map = new GroupFilesMap();

        // Iterate through each file, match against the reg-exp and populate a hash-map
        Iterator<FileInput> itrFiles = fileInput.inputs(params).iterator();
        while (itrFiles.hasNext()) {

            FileInput f = itrFiles.next();

            String path = f.getFile().getAbsolutePath();
            path = path.replaceAll("\\\\", "/");

            if (filePathParser.setPath(path)) {
                FileDetails fd =
                        new FileDetails(
                                Paths.get(path),
                                filePathParser.getChannelNum(),
                                filePathParser.getZSliceNum(),
                                filePathParser.getTimeIndex());
                map.add(filePathParser.getKey(), fd);
            } else {
                if (requireAllFilesMatch) {
                    throw new AnchorIOException(
                            String.format("File %s did not match parser", path));
                }
            }
        }
        return listFromMap(map, params.getLogger());
    }

    private List<NamedChannelsInput> listFromMap(GroupFilesMap map, Logger logger)
            throws AnchorIOException {

        List<File> files = new ArrayList<>();
        List<MultiFileReaderOpenedRaster> openedRasters = new ArrayList<>();

        // Process the hash-map by key
        for (String key : map.keySet()) {
            ParsedFilePathBag bag = map.get(key);
            assert (bag != null);

            // If we have a condition to check against
            if (checkParsedFilePathBag != null && !checkParsedFilePathBag.accept(bag)) {
                continue;
            }

            files.add(Paths.get(key).toFile());
            openedRasters.add(new MultiFileReaderOpenedRaster(rasterReader, bag));
        }

        return zipIntoGrouping(
                descriptiveName.describeCheckUnique(files, logger),
                openedRasters);
    }

    private List<NamedChannelsInput> zipIntoGrouping(
            List<DescriptiveFile> df, List<MultiFileReaderOpenedRaster> or) {

        Iterator<DescriptiveFile> it1 = df.iterator();
        Iterator<MultiFileReaderOpenedRaster> it2 = or.iterator();

        List<NamedChannelsInput> result = new ArrayList<>();
        while (it1.hasNext() && it2.hasNext()) {
            DescriptiveFile d = it1.next();
            result.add(new GroupingInput(d.getFile().toPath(), it2.next(), imgChannelMapCreator));
        }
        return result;
    }
}
