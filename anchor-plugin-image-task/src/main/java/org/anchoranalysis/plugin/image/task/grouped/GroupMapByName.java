/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.grouped;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.identifier.name.MapCreate;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.feature.io.name.MultiName;
import org.anchoranalysis.feature.io.name.MultiNameFactory;
import org.anchoranalysis.io.manifest.ManifestDirectoryDescription;
import org.anchoranalysis.io.manifest.sequencetype.StringsWithoutOrder;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.InputOutputContextSubdirectoryCache;

/**
 * Adds items to aggregate structures identified uniquely by a name, and allows these items to be
 * later outputted.
 *
 * <p>If any of the {@link MultiName} have a higher level aggregation-key, this is used to partition
 * the output at the end into sub-directories.
 *
 * @author Owen Feehan
 * @param <S> individual-item type
 * @param <T> aggregate-item type (combines many individual types)
 */
public abstract class GroupMapByName<S, T> {

    private final MapCreate<MultiName, T> map;

    private final String nounT;

    private final ManifestDirectoryDescription manifestDirectoryDescription;

    /**
     * Creates a group-map.
     *
     * @param nounT a word to describe a single instance of T in user error messages
     * @param createEmpty
     */
    protected GroupMapByName(String nounT, String manifestFunction, Supplier<T> createEmpty) {
        this.map = new MapCreate<>(createEmpty);
        this.nounT = nounT;
        this.manifestDirectoryDescription =
                new ManifestDirectoryDescription(
                        "groupedFolder", manifestFunction, new StringsWithoutOrder());
    }

    /**
     * Adds an item with an identifier
     *
     * @throws JobExecutionException
     */
    public synchronized void add(
            Optional<String> groupIdentifier, String nonGroupIdentifier, S itemToAdd)
            throws JobExecutionException {

        MultiName identifier = MultiNameFactory.create(groupIdentifier, nonGroupIdentifier);

        try {
            addTo(itemToAdd, map.computeIfAbsent(identifier));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(
                    String.format(
                            "An error occurred combining the %s created for: %s",
                            nounT, identifier),
                    e);
        }
    }

    /**
     * Outputs the "grouped" data to the filesystem
     *
     * @param channelChecker channel checker
     * @param context
     * @throws IOException if something goes wrong, or if includeGroupName is false, but more than
     *     one group-names exist
     */
    public void outputGroupedData(
            ConsistentChannelChecker channelChecker, InputOutputContext context)
            throws IOException {

        // We wish to create a new output-manager only once for each primary key, so we store them
        // in a hashmap
        InputOutputContextSubdirectoryCache subdirectoryCache =
                new InputOutputContextSubdirectoryCache(
                        context, manifestDirectoryDescription, false);

        // If there is one part-only, it is assumed that there is no group (for all items) and it is
        // written without a subdirectory
        // If there are two parts, it is assumed that the first-part is a group-name (a separate
        // subdirectory) and the second-part is written without a subdirectory
        for (Entry<MultiName, T> entry : map.entrySet()) {

            MultiName name = entry.getKey();

            writeGroupOutputInSubdirectory(
                    name.secondPart(),
                    entry.getValue(),
                    channelChecker,
                    subdirectoryCache.get(name.firstPart()));
        }
    }

    protected abstract void addTo(S ind, T agg) throws OperationFailedException;

    protected abstract void writeGroupOutputInSubdirectory(
            String partName,
            T agg,
            ConsistentChannelChecker channelChecker,
            InputOutputContext context)
            throws IOException;
}
