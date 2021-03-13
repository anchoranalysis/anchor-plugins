/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.mpp.experiment.bean;

import com.owenfeehan.pathpatternfinder.commonpath.FindCommonPathElements;
import com.owenfeehan.pathpatternfinder.commonpath.PathElements;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputFromManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CommonRootHelper {

    /**
     * Finds a maximal common path that is the parent of the {@link
     * InputFromManager#pathForBinding()} associated with each input.
     *
     * @param <T> input-type in the list
     * @param inputs the inputs
     * @return a path that is the parent of all files
     * @throws ExperimentExecutionException if no such path can be identified.
     */
    public static <T extends NamedChannelsInput> Path findCommonPathRoot(List<T> inputs)
            throws ExperimentExecutionException {
        return findCommonPathRootOptional(inputs)
                .orElseThrow(
                        () ->
                                new ExperimentExecutionException(
                                        "No common root exists for the paths, so cannot copy."));
    }

    /** Finds the common root of a list of inputs. */
    private static <T extends NamedChannelsInput> Optional<Path> findCommonPathRootOptional(
            List<T> inputs) {

        List<Path> paths = extractPaths(inputs);

        if (paths.size() == 1) {
            return Optional.ofNullable(paths.get(0).getParent());
        } else {
            return FindCommonPathElements.findForFilePaths(paths).map(PathElements::toPath);
        }
    }

    private static <T extends NamedChannelsInput> List<Path> extractPaths(List<T> inputs) {
        return inputs.stream()
                .map(InputFromManager::pathForBinding)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CommonRootHelper::normalizePath)
                .collect(Collectors.toList());
    }

    private static Path normalizePath(Path pathUnormalized) {
        return pathUnormalized.toAbsolutePath().normalize();
    }
}
