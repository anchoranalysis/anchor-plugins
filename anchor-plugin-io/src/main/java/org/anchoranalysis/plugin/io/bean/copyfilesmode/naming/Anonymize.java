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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.plugin.io.shared.AnonymizeSharedState;
import org.anchoranalysis.plugin.io.shared.NumberToStringConverter;

/**
 * Copies files to a number 001 002 etc. in the same order they are inputted.
 *
 * <p>No shuffling occurs.
 *
 * @author feehano
 */
public class Anonymize extends CopyFilesNaming<AnonymizeSharedState> {

    @Override
    public AnonymizeSharedState beforeCopying(Path destinationDirectory, int totalNumberFiles) {
        return new AnonymizeSharedState(
                new NumberToStringConverter(totalNumberFiles),
                createMappingToShuffledIndices(totalNumberFiles));
    }

    @Override
    public Optional<Path> destinationPathRelative(
            Path sourceDirectory,
            Path destinationDirectory,
            File file,
            int iter,
            AnonymizeSharedState sharedState)
            throws OutputWriteFailedException {
        Integer mappedIteration = sharedState.getMapping().get(iter);
        if (mappedIteration == null) {
            throw new OutputWriteFailedException(
                    "An unexpected value was passed as iteration, and no mapping is available: "
                            + iter);
        }

        Optional<String> extension = ExtensionUtilities.extractExtension(file.toString());

        String filenameToCopyTo =
                ExtensionUtilities.appendExtension(
                        sharedState.getNumberConverter().convert(mappedIteration), extension);
        return Optional.of(Paths.get(filenameToCopyTo));
    }

    private static Map<Integer, Integer> createMappingToShuffledIndices(int totalNumberFiles) {
        List<Integer> indices = createSequence(totalNumberFiles);
        Collections.shuffle(indices);
        return mapIndexToElement(indices);
    }

    /**
     * Creates a list with a sequence of Integers from 0 to {@code maxNumberExclusive - 1}
     * (inclusive).
     */
    private static List<Integer> createSequence(int maxNumberExclusive) {
        return IntStream.range(0, maxNumberExclusive).boxed().collect(Collectors.toList());
    }

    private static <T> Map<Integer, T> mapIndexToElement(List<T> list) {
        Map<Integer, T> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(i, list.get(i));
        }
        return map;
    }
}
