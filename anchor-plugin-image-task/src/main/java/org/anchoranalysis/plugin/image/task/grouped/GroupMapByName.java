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
import org.anchoranalysis.core.collection.MapCreate;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.feature.io.csv.name.MultiName;
import org.anchoranalysis.feature.io.csv.name.MultiNameFactory;
import org.anchoranalysis.io.manifest.ManifestFolderDescription;
import org.anchoranalysis.io.manifest.sequencetype.SetSequenceType;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.CacheSubdirectoryContext;

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

    private MapCreate<MultiName, T> map;

    private String nounT;

    private final ManifestFolderDescription manifestFolderDescription;

    /**
     * @param nounT a word to describe a single instance of T in user error messages
     * @param createEmpty
     */
    public GroupMapByName(
            String nounT,
            String manifestFunction,
            Supplier<T> createEmpty) {
        super();
        this.map = new MapCreate<>(createEmpty);
        this.nounT = nounT;
        this.manifestFolderDescription =
                new ManifestFolderDescription(
                        "groupedFolder", manifestFunction, new SetSequenceType());
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
            addTo(itemToAdd, map.getOrCreateNew(identifier));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(
                    String.format(
                            "An error occurred combining the %s created for: %s",
                            nounT, identifier),
                    e);
        }
    }

    /**
     * Outputs the "grouped" data to the file-system
     *
     * @param chnlChecker channel checker
     * @param context
     * @throws IOException if something goes wrong, or if includeGroupName is FALSE, but more than
     *     one group-names exist
     */
    public void outputGroupedData(ConsistentChannelChecker chnlChecker, BoundIOContext context)
            throws IOException {

        // We wish to create a new output-manager only once for each primary key, so we store them
        // in a hashmap
        CacheSubdirectoryContext subdirectoryCache =
                new CacheSubdirectoryContext(context, manifestFolderDescription);

        // If there is one part-only, it is assumed that there is no group (for all items) and it is
        // written without a subdirectory
        // If there are two parts, it is assumed that the first-part is a group-name (a seperate
        // subdirectory) and the second-part is written without a subdirectory
        for (Entry<MultiName, T> entry : map.entrySet()) {

            MultiName name = entry.getKey();

            writeGroupOutputInSubdirectory(
                    name.filePart(),
                    entry.getValue(),
                    chnlChecker,
                    subdirectoryCache.get(name.directoryPart()));
        }
    }

    protected abstract void addTo(S ind, T agg) throws OperationFailedException;

    protected abstract void writeGroupOutputInSubdirectory(
            String outputName, T agg, ConsistentChannelChecker chnlChecker, BoundIOContext context)
            throws IOException;
}
