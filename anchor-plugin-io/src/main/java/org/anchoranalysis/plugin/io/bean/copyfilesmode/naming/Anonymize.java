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

package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.system.path.PathDifferenceException;
import org.anchoranalysis.core.value.TypedValue;
import org.anchoranalysis.io.generator.tabular.CSVWriter;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.apache.commons.io.FilenameUtils;

/**
 * Copies files to a number 001 002 etc. in the same order they are inputted.
 *
 * <p>No shuffling occurs.
 *
 * @author feehano
 */
public class Anonymize implements CopyFilesNaming {

    private static final String OUTPUT_CSV_FILENAME = "output.csv";

    @Value
    private static class FileMapping {
        private final Path original;
        private final String anonymized;
        private final int iter;
    }

    // START BEAN PROPERTIES
    /**
     * Iff true, a mapping.csv file is created showing the mapping between the original-names and
     * the anonymized versions
     */
    @BeanField @Getter @Setter private boolean outputCSV = true;
    // END BEAN PROPERTIES

    private Optional<List<FileMapping>> optionalMappings;
    private String formatStr;

    @Override
    public void beforeCopying(Path destDir, int totalNumFiles) {
        optionalMappings = OptionalUtilities.createFromFlag(outputCSV, ArrayList::new);

        formatStr = createFormatStrForMaxNum(totalNumFiles);
    }

    @Override
    public Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int iter)
            throws OutputWriteFailedException {
        String ext = FilenameUtils.getExtension(file.toString());
        String fileNameNew = createNumericString(iter) + "." + ext;

        if (optionalMappings.isPresent()) {
            try {
                addMapping(optionalMappings.get(), sourceDir, file, iter, fileNameNew);
            } catch (PathDifferenceException e) {
                throw new OutputWriteFailedException(e);
            }
        }

        return Optional.of(Paths.get(fileNameNew));
    }

    @Override
    public void afterCopying(Path destDir, boolean dummyMode) throws OutputWriteFailedException {

        if (optionalMappings.isPresent() && !dummyMode) {
            writeOutputCSV(optionalMappings.get(), destDir);
            optionalMappings = Optional.empty();
        }
    }

    private void addMapping(
            List<FileMapping> mapping, Path sourceDir, File file, int iter, String fileNameNew)
            throws PathDifferenceException {
        synchronized (this) {
            mapping.add( // NOSONAR
                    new FileMapping(
                            NamingUtilities.filePathDifference(sourceDir, file.toPath()),
                            fileNameNew,
                            iter));
        }
    }

    private void writeOutputCSV(List<FileMapping> listMappings, Path destDir)
            throws OutputWriteFailedException {

        Path csvOut = destDir.resolve(OUTPUT_CSV_FILENAME);

        CSVWriter csvWriter = CSVWriter.create(csvOut);

        csvWriter.writeHeaders(Arrays.asList("iter", "in", "out"));

        try {
            for (FileMapping mapping : listMappings) {
                csvWriter.writeRow(
                        Arrays.asList(
                                new TypedValue(mapping.getIter()),
                                new TypedValue(mapping.getOriginal().toString()),
                                new TypedValue(mapping.getAnonymized())));
            }
        } finally {
            csvWriter.close();
        }
    }

    private String createNumericString(int iter) {
        return String.format(formatStr, iter);
    }

    private static String createFormatStrForMaxNum(int maxNum) {
        int maxNumDigits = (int) Math.ceil(Math.log10(maxNum));

        if (maxNumDigits > 0) {
            return "%0" + maxNumDigits + "d";
        } else {
            return "%d";
        }
    }
}
